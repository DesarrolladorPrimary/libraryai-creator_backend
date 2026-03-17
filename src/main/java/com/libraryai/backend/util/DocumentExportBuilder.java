package com.libraryai.backend.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Construye archivos exportables a partir del contenido final de un relato.
 *
 * <p>Ofrece dos salidas: un documento HTML compatible con Word y un PDF simple
 * maquetado directamente con PDFBox.
 */
public class DocumentExportBuilder {

    private static final PDType1Font TITLE_FONT = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
    private static final PDType1Font BODY_FONT = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
    private static final float TITLE_FONT_SIZE = 18f;
    private static final float BODY_FONT_SIZE = 12f;
    private static final float MARGIN = 56f;
    private static final float LEADING = 18f;

    /**
     * Genera un documento tipo Word basado en HTML para exportaciones ligeras.
     */
    public static byte[] buildWordDocument(String title, String content) {
        String html = """
                <!DOCTYPE html>
                <html lang="es">
                  <head>
                    <meta charset="UTF-8" />
                    <title>%s</title>
                    <style>
                      body { font-family: Georgia, serif; margin: 48px; line-height: 1.7; color: #222; }
                      h1 { margin-bottom: 32px; font-size: 28px; }
                      p { margin: 0 0 16px; }
                    </style>
                  </head>
                  <body>
                    <h1>%s</h1>
                    %s
                  </body>
                </html>
                """.formatted(
                escapeHtml(title),
                escapeHtml(title),
                buildHtmlParagraphs(content));

        return html.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Renderiza un PDF paginado básico con título y cuerpo del relato.
     */
    public static byte[] buildPdfDocument(String title, String content) throws IOException {
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            float width = page.getMediaBox().getWidth() - (MARGIN * 2);
            float y = page.getMediaBox().getHeight() - MARGIN;

            PDPageContentStream stream = new PDPageContentStream(document, page);

            y = writeLine(stream, y, title, TITLE_FONT, TITLE_FONT_SIZE);
            y -= LEADING;

            for (String paragraph : splitParagraphs(content)) {
                List<String> lines = wrapLines(paragraph, BODY_FONT, BODY_FONT_SIZE, width);

                for (String line : lines) {
                    if (y <= MARGIN) {
                        stream.close();
                        page = new PDPage(PDRectangle.LETTER);
                        document.addPage(page);
                        stream = new PDPageContentStream(document, page);
                        y = page.getMediaBox().getHeight() - MARGIN;
                    }

                    y = writeLine(stream, y, line, BODY_FONT, BODY_FONT_SIZE);
                }

                y -= LEADING * 0.5f;
            }

            stream.close();
            document.save(output);
            return output.toByteArray();
        }
    }

    /**
     * Escribe una línea en la posición vertical actual y devuelve la siguiente
     * coordenada disponible.
     */
    private static float writeLine(PDPageContentStream stream, float y, String text, PDType1Font font,
            float fontSize) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(MARGIN, y);
        stream.showText(text);
        stream.endText();
        return y - LEADING;
    }

    /**
     * Ajusta párrafos a un ancho máximo sin depender de un motor de maquetación
     * externo.
     */
    private static List<String> wrapLines(String text, PDType1Font font, float fontSize, float maxWidth)
            throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = String.valueOf(text).trim().split("\\s+");

        if (words.length == 0 || (words.length == 1 && words[0].isBlank())) {
            lines.add("");
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            float candidateWidth = font.getStringWidth(candidate) / 1000 * fontSize;

            if (candidateWidth <= maxWidth || currentLine.isEmpty()) {
                currentLine.setLength(0);
                currentLine.append(candidate);
            } else {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLine.append(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Separa el contenido en párrafos lógicos tomando dobles saltos de línea como
     * delimitador principal.
     */
    private static List<String> splitParagraphs(String content) {
        String normalized = String.valueOf(content).trim();
        String[] paragraphs = normalized.split("\\n\\s*\\n");
        List<String> list = new ArrayList<>();

        for (String paragraph : paragraphs) {
            String value = paragraph.trim();
            if (!value.isBlank()) {
                list.add(value);
            }
        }

        if (list.isEmpty()) {
            list.add(normalized);
        }

        return list;
    }

    /**
     * Convierte el contenido del relato a un bloque HTML seguro con párrafos.
     */
    private static String buildHtmlParagraphs(String content) {
        StringBuilder builder = new StringBuilder();

        for (String paragraph : splitParagraphs(content)) {
            builder.append("<p>")
                    .append(escapeHtml(paragraph))
                    .append("</p>");
        }

        return builder.toString();
    }

    /**
     * Escapa caracteres especiales para evitar romper el documento HTML generado.
     */
    private static String escapeHtml(String value) {
        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
