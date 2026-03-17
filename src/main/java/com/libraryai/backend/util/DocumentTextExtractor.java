package com.libraryai.backend.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

/**
 * Extrae texto plano desde documentos fuente soportados por Poly.
 *
 * <p>Se usa para construir contexto resumido a partir de archivos PDF, DOC o
 * DOCX previamente guardados por el usuario.
 */
public class DocumentTextExtractor {

    private static final int MAX_EXTRACTED_CHARS = 12000;

    /**
     * Abre un archivo almacenado en disco y devuelve una versión textual acotada.
     */
    public static String extractText(String storagePath, String fileType) {
        if (storagePath == null || storagePath.isBlank() || fileType == null || fileType.isBlank()) {
            return "";
        }

        Path path = resolvePath(storagePath);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            return "";
        }

        try {
            String normalizedType = fileType.trim().toUpperCase();

            return switch (normalizedType) {
                case "PDF" -> limit(extractPdfText(path));
                case "DOCX" -> limit(extractDocxText(path));
                case "DOC" -> limit(extractDocText(path));
                default -> "";
            };
        } catch (Exception e) {
            System.err.println("No fue posible extraer texto del documento: " + e.getMessage());
            return "";
        }
    }

    /**
     * Resuelve rutas relativas respecto al workspace actual para poder reutilizar
     * registros almacenados por el backend.
     */
    private static Path resolvePath(String storagePath) {
        Path path = Paths.get(storagePath);
        if (path.isAbsolute()) {
            return path.normalize();
        }

        return Paths.get("").toAbsolutePath().resolve(storagePath).normalize();
    }

    /**
     * Extrae texto de documentos PDF usando PDFBox.
     */
    private static String extractPdfText(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Extrae texto de documentos Word modernos (`.docx`).
     */
    private static String extractDocxText(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path);
                XWPFDocument document = new XWPFDocument(input);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * Extrae texto de documentos Word binarios clásicos (`.doc`).
     */
    private static String extractDocText(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path);
                HWPFDocument document = new HWPFDocument(input);
                WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * Recorta el texto extraído para no desbordar el contexto enviado al modelo.
     */
    private static String limit(String rawText) {
        String normalized = rawText == null ? "" : rawText.replace("\u0000", "").trim();
        if (normalized.length() <= MAX_EXTRACTED_CHARS) {
            return normalized;
        }

        return normalized.substring(0, MAX_EXTRACTED_CHARS);
    }
}
