package com.libraryai.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.ModerationDao;

/**
 * Moderación básica de texto basada en la tabla PalabraProhibida.
 */
public class ModerationService {

    public static JsonObject validateText(String text, int userId, String userMessage, String logReason) {
        String normalizedText = normalize(text);
        if (normalizedText.isBlank()) {
            return null;
        }

        if (containsExplicitMarkers(normalizedText) || containsForbiddenWord(normalizedText, ModerationDao.listForbiddenWords())) {
            ModerationDao.createModerationLog(userId, logReason, hash(text));

            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", userMessage);
            response.addProperty("status", 400);
            return response;
        }

        return null;
    }

    private static boolean containsExplicitMarkers(String normalizedText) {
        return normalizedText.contains("+18") || normalizedText.contains("nsfw");
    }

    private static boolean containsForbiddenWord(String normalizedText, List<String> forbiddenWords) {
        for (String rawWord : forbiddenWords) {
            String normalizedWord = normalize(rawWord);
            if (normalizedWord.isBlank()) {
                continue;
            }

            if (normalizedWord.contains(" ")) {
                if (normalizedText.contains(normalizedWord)) {
                    return true;
                }
                continue;
            }

            Pattern pattern = Pattern.compile("(?iu)\\b" + Pattern.quote(normalizedWord) + "\\b");
            if (pattern.matcher(normalizedText).find()) {
                return true;
            }
        }

        return false;
    }

    private static String normalize(String value) {
        String normalized = String.valueOf(value == null ? "" : value).trim().toLowerCase();
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "");
    }

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
}
