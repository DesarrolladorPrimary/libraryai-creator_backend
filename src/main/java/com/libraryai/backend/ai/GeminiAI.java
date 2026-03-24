package com.libraryai.backend.ai;

import com.google.gson.JsonObject;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import com.libraryai.backend.config.AIConfig;

/**
 * Cliente para generar contenido con Gemini.
 */
public class GeminiAI {

    private static final String CODE_GEMINI_MISCONFIGURED = "GEMINI_MISCONFIGURED";
    private static final String CODE_GEMINI_EMPTY_RESPONSE = "GEMINI_EMPTY_RESPONSE";
    private static final String CODE_GEMINI_TIMEOUT = "GEMINI_TIMEOUT";
    private static final String CODE_GEMINI_RATE_LIMIT = "GEMINI_RATE_LIMIT";
    private static final String CODE_GEMINI_UNAVAILABLE = "GEMINI_UNAVAILABLE";

    /**
     * Llama al SDK de Gemini para generar texto a partir de un prompt.
     */
    public static JsonObject generateText(String responseUser, String instruccionesUser) {
        return generateText(responseUser, instruccionesUser, AIConfig.MODEL_AI);
    }

    /**
     * Llama al SDK de Gemini usando un modelo específico.
     */
    public static JsonObject generateText(String responseUser, String instruccionesUser, String modelName) {
        JsonObject response = new JsonObject();
        String prompt = String.valueOf(responseUser == null ? "" : responseUser).trim();
        String instructions = String.valueOf(instruccionesUser == null ? "" : instruccionesUser).trim();
        String runtimeModel = String.valueOf(modelName == null ? "" : modelName).trim();

        if (runtimeModel.isBlank()) {
            runtimeModel = AIConfig.MODEL_AI;
        }

        if (AIConfig.API_KEY == null || AIConfig.API_KEY.isBlank()) {
            response.addProperty("Mensaje", "GEMINI_API_KEY no está configurada");
            response.addProperty("status", 500);
            response.addProperty("code", CODE_GEMINI_MISCONFIGURED);
            return response;
        }

        if (prompt.isBlank()) {
            response.addProperty("Mensaje", "El prompt para Gemini está vacío");
            response.addProperty("status", 400);
            return response;
        }

        if (prompt.length() > AIConfig.MAX_INPUT_CHARS) {
            response.addProperty("Mensaje", "El prompt supera el límite permitido para Gemini");
            response.addProperty("status", 400);
            response.addProperty("detalle", "max_input_chars=" + AIConfig.MAX_INPUT_CHARS);
            return response;
        }

        try (
            // Crea el cliente con la API key configurada.
            Client client = Client.builder()
                    .apiKey(AIConfig.API_KEY)
                    .httpOptions(HttpOptions.builder().timeout(AIConfig.TIMEOUT_MS).build())
                    .build();
        ){
            HttpOptions httpOptions = HttpOptions.builder().timeout(AIConfig.TIMEOUT_MS).build();
            GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder()
                    .httpOptions(httpOptions);

            if (!instructions.isBlank()) {
                Content system = Content.builder()
                        .role("system")
                        .parts(Part.fromText(instructions))
                        .build();
                configBuilder.systemInstruction(system);
            }

            if (AIConfig.MAX_OUTPUT_TOKENS > 0) {
                configBuilder.maxOutputTokens(AIConfig.MAX_OUTPUT_TOKENS);
            }

            GenerateContentConfig cfg = configBuilder.build();

            System.out.println("Solicitando a Gemini con modelo " + runtimeModel
                    + " y timeout " + AIConfig.TIMEOUT_MS + "ms");

            // Llama al modelo con prompt y configuracion.
            GenerateContentResponse resp = 
            client.models.generateContent(
                runtimeModel, 
                prompt, 
                cfg
            );
            
            String generatedText = resp.text();
            if (generatedText == null || generatedText.isBlank()) {
                response.addProperty("Mensaje", "Gemini devolvió una respuesta vacía");
                response.addProperty("status", 502);
                response.addProperty("code", CODE_GEMINI_EMPTY_RESPONSE);
                response.addProperty("detalle", "Respuesta vacía del modelo " + runtimeModel);
                response.addProperty("modelo", runtimeModel);
                return response;
            }

            response.addProperty("AI", generatedText);
            response.addProperty("status", 200);
            response.addProperty("modelo", runtimeModel);
            return response;


        } catch (RuntimeException e) {
            String detail = buildErrorDetail(e);
            GeminiFailure failure = classifyFailure(detail);

            System.err.println("Error Gemini [" + e.getClass().getSimpleName() + "]: " + detail);
            response.addProperty("Mensaje", failure.userMessage);
            response.addProperty("status", failure.status);
            response.addProperty("code", failure.code);
            response.addProperty("detalle", detail);
            response.addProperty("modelo", runtimeModel);
            return response;
        }
    }

    private static GeminiFailure classifyFailure(String detail) {
        String normalized = detail == null ? "" : detail.toLowerCase();

        if (containsAny(normalized, "timeout", "timed out", "deadline exceeded")) {
            return new GeminiFailure(
                    504,
                    CODE_GEMINI_TIMEOUT,
                    "Gemini tardó demasiado en responder. Intenta de nuevo en unos segundos.");
        }

        if (containsAny(normalized,
                "429",
                "resource_exhausted",
                "rate limit",
                "quota",
                "exceeded your current quota",
                "too many requests")) {
            return new GeminiFailure(
                    429,
                    CODE_GEMINI_RATE_LIMIT,
                    "Se agotó temporalmente la cuota de Gemini. Intenta de nuevo más tarde.");
        }

        return new GeminiFailure(
                502,
                CODE_GEMINI_UNAVAILABLE,
                "Error al comunicarse con Gemini");
    }

    private static boolean containsAny(String source, String... tokens) {
        for (String token : tokens) {
            if (source.contains(token)) {
                return true;
            }
        }

        return false;
    }

    private static String buildErrorDetail(RuntimeException error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return error.getClass().getSimpleName();
        }

        return error.getClass().getSimpleName() + ": " + message;
    }

    private static final class GeminiFailure {
        private final int status;
        private final String code;
        private final String userMessage;

        private GeminiFailure(int status, String code, String userMessage) {
            this.status = status;
            this.code = code;
            this.userMessage = userMessage;
        }
    }

}
