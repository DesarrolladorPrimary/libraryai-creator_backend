package com.libraryai.backend.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuracion de cliente IA.
 */
public class AIConfig {
    public static final Dotenv env = Dotenv.load();
    
    // API key para el SDK de Gemini.
    public static final String API_KEY = env.get("GEMINI_API_KEY");
    // Nombre o id del modelo a usar.
    public static final String MODEL_AI = env.get("GEMINI_MODEL", "gemini-2.0-flash");
    // Configuracion opcional de limites y timeout.
    public static final int TIMEOUT_MS = parseInteger(env.get("AI_TIMEOUT_MS"), 15000);
    public static final int MAX_INPUT_CHARS = parseInteger(env.get("AI_MAX_INPUT_CHARS"), 20000);
    public static final int MAX_OUTPUT_TOKENS = parseInteger(env.get("AI_MAX_OUTPUT_TOKEN"), 1024);

    private static int parseInteger(String rawValue, int defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
