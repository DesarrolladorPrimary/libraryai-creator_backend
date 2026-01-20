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
    public static final String MODEL_AI = env.get("GEMINI_MODEL");
    // Configuracion opcional de limites (no usada en este archivo).
    public static final String AI_TIMEOUT_MS = env.get("AI_TIMEOUT_MS");
    public static final String AI_MAX_INPUT_CHARS = env.get("AI_MAX_INPUT_CHARS");
    public static final String AI_MAX_OUTPUT_TOKEN = env.get("AI_MAX_OUTPUT_TOKEN");
}
