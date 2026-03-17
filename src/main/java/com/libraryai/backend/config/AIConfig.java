package com.libraryai.backend.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Centraliza la lectura de configuración para el cliente de IA.
 *
 * <p>Expone valores listos para usar por servicios y seeders, aplicando valores
 * por defecto y compatibilidad con variables heredadas del proyecto.
 */
public class AIConfig {
    public static final Dotenv env = Dotenv.load();
    private static final String LEGACY_MODEL = normalizeModelName(env.get("GEMINI_MODEL"), "gemini-2.5-flash");
    
    // API key para el SDK de Gemini.
    public static final String API_KEY = env.get("GEMINI_API_KEY");
    // Modelo base para el plan Gratuito.
    public static final String FREE_MODEL = normalizeModelName(env.get("GEMINI_FREE_MODEL"), LEGACY_MODEL);
    // Modelo preferente para el plan Premium.
    public static final String PREMIUM_MODEL = normalizeModelName(env.get("GEMINI_PREMIUM_MODEL"), "gemini-2.5-pro");
    // Alias legado usado por partes antiguas del backend.
    public static final String MODEL_AI = FREE_MODEL;
    // Configuración opcional de límites y timeout.
    public static final int TIMEOUT_MS = parseInteger(env.get("AI_TIMEOUT_MS"), 15000);
    public static final int MAX_INPUT_CHARS = parseInteger(env.get("AI_MAX_INPUT_CHARS"), 20000);
    public static final int MAX_OUTPUT_TOKENS = parseInteger(env.get("AI_MAX_OUTPUT_TOKEN"), 1024);

    /**
     * Limpia el nombre del modelo recibido por entorno y aplica fallback.
     */
    private static String normalizeModelName(String rawValue, String defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }

        return rawValue.trim();
    }

    /**
     * Convierte enteros configurables por entorno sin romper el arranque si el
     * valor viene ausente o mal formado.
     */
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
