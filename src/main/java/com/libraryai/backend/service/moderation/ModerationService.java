package com.libraryai.backend.service.moderation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.moderation.ModerationDao;

/**
 * Servicio de moderación básica de texto.
 *
 * <p>Normaliza el texto recibido, detecta marcadores explícitos y compara contra
 * la blacklist persistida. Si detecta contenido prohibido, responde con error y
 * registra trazabilidad en auditoría.
 */
public class ModerationService {

    /**
     * Valida un texto de entrada y devuelve una respuesta de error lista para el
     * controlador si el contenido no está permitido.
     *
     * @return {@code null} cuando el texto es válido; un {@link JsonObject} con
     *         estado 400 cuando debe bloquearse.
     */
    public static JsonObject validateText(String text, int userId, String userMessage, String logReason) {
        TextVariants variants = buildVariants(text);
        if (variants.spaced.isBlank() && variants.collapsed.isBlank()) {
            return null;
        }

        if (containsExplicitMarkers(variants) || containsForbiddenWord(variants, ModerationDao.listForbiddenWords())) {
            ModerationDao.createModerationLog(userId, logReason, hash(text));

            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", userMessage);
            response.addProperty("status", 400);
            return response;
        }

        return null;
    }

    /**
     * Detecta marcadores explícitos conocidos incluso después de la normalización
     * de espacios y caracteres ofuscados.
     */
    private static boolean containsExplicitMarkers(TextVariants variants) {
        if (variants == null) {
            return false;
        }

        if (Pattern.compile("(?iu)(?:^|\\s)(?:\\+\\s*18|18\\s*\\+)(?:$|\\s)")
                .matcher(variants.spaced)
                .find()) {
            return true;
        }

        String collapsed = variants.collapsed;
        return collapsed.contains("nsfw")
                || collapsed.contains("porno")
                || collapsed.contains("pornografia")
                || collapsed.contains("sexoexplicito")
                || collapsed.contains("contenidoadulto")
                || collapsed.contains("xxx");
    }

    /**
     * Busca coincidencias de la blacklist tanto en versión espaciada como en su
     * variante colapsada para cubrir ofuscaciones simples.
     */
    private static boolean containsForbiddenWord(TextVariants variants, List<String> forbiddenWords) {
        if (variants == null) {
            return false;
        }

        for (String rawWord : forbiddenWords) {
            TextVariants wordVariants = buildVariants(rawWord);
            if (wordVariants.spaced.isBlank() && wordVariants.collapsed.isBlank()) {
                continue;
            }

            if (wordVariants.spaced.contains(" ")) {
                if (variants.spaced.contains(wordVariants.spaced)
                        || (!wordVariants.collapsed.isBlank() && variants.collapsed.contains(wordVariants.collapsed))) {
                    return true;
                }
                continue;
            }

            Pattern pattern = Pattern.compile("(?iu)\\b" + Pattern.quote(wordVariants.spaced) + "\\b");
            if (pattern.matcher(variants.spaced).find()) {
                return true;
            }

            if (!wordVariants.collapsed.isBlank() && variants.collapsed.contains(wordVariants.collapsed)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Construye dos representaciones del texto:
     * una legible para búsquedas por palabra y otra colapsada para detectar
     * variantes con separadores o símbolos intermedios.
     */
    private static TextVariants buildVariants(String value) {
        String normalized = String.valueOf(value == null ? "" : value).trim().toLowerCase();
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}+", "");

        StringBuilder canonical = new StringBuilder(normalized.length());
        for (char current : normalized.toCharArray()) {
            canonical.append(mapLeetCharacter(current));
        }

        String spaced = canonical
                .toString()
                .replaceAll("[^a-z0-9+]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        String collapsed = spaced.replace(" ", "");
        return new TextVariants(spaced, collapsed);
    }

    /**
     * Traduce sustituciones comunes usadas para disimular palabras prohibidas.
     */
    private static char mapLeetCharacter(char current) {
        return switch (current) {
            case '@', '4' -> 'a';
            case '3' -> 'e';
            case '1', '!', '|', 'í', 'ì', 'ï', 'î' -> 'i';
            case '0' -> 'o';
            case '$', '5' -> 's';
            case '7' -> 't';
            case '8' -> 'b';
            default -> current;
        };
    }

    /**
     * Genera un hash del contenido bloqueado para auditar intentos sin guardar el
     * texto sensible en claro.
     */
    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);

            for (byte current : bytes) {
                builder.append(String.format("%02x", current));
            }

            return builder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Contenedor inmutable con las variantes normalizadas usadas por la detección.
     */
    private static final class TextVariants {
        private final String spaced;
        private final String collapsed;

        private TextVariants(String spaced, String collapsed) {
            this.spaced = spaced == null ? "" : spaced;
            this.collapsed = collapsed == null ? "" : collapsed;
        }
    }
}
