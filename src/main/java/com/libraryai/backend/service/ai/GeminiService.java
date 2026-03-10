package com.libraryai.backend.service.ai;

import java.util.LinkedHashSet;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.ai.GeminiAI;
import com.libraryai.backend.config.AIConfig;

/**
 * Servicio que integra Gemini AI.
 */
public class GeminiService {
    
    /**
     * Valida el input, llama al cliente Gemini y normaliza la respuesta.
     */
    public static JsonObject generateText(String mensaje, String instrucciones){
        JsonObject responseAI = new JsonObject();
        String prompt = mensaje != null ? mensaje.trim() : "";
        
        // No se puede generar respuesta sin prompt.
        if (prompt.isEmpty()) {
            responseAI.addProperty("Mensaje", "El mensaje esta vacio");
            responseAI.addProperty("status", 400);
            return responseAI;
        }

        // Llama al cliente de IA con el prompt e instrucciones.
        return GeminiAI.generateText(prompt, instrucciones);
    }

    public static JsonObject generateText(String mensaje, String instrucciones, List<String> candidateModels) {
        JsonObject responseAI = new JsonObject();
        String prompt = mensaje != null ? mensaje.trim() : "";

        if (prompt.isEmpty()) {
            responseAI.addProperty("Mensaje", "El mensaje esta vacio");
            responseAI.addProperty("status", 400);
            return responseAI;
        }

        LinkedHashSet<String> modelsToTry = new LinkedHashSet<>();
        if (candidateModels != null) {
            for (String model : candidateModels) {
                String normalized = model == null ? "" : model.trim();
                if (!normalized.isBlank()) {
                    modelsToTry.add(normalized);
                }
            }
        }

        if (AIConfig.MODEL_AI != null && !AIConfig.MODEL_AI.isBlank()) {
            modelsToTry.add(AIConfig.MODEL_AI.trim());
        }

        if (modelsToTry.isEmpty()) {
            return GeminiAI.generateText(prompt, instrucciones);
        }

        JsonArray attempts = new JsonArray();
        JsonObject lastError = null;

        for (String model : modelsToTry) {
            JsonObject modelResponse = GeminiAI.generateText(prompt, instrucciones, model);
            int status = modelResponse.has("status") ? modelResponse.get("status").getAsInt() : 500;

            if (status == 200) {
                if (attempts.size() > 0) {
                    modelResponse.add("modelosIntentados", attempts);
                }
                return modelResponse;
            }

            if (status == 400 || status == 500) {
                return modelResponse;
            }

            JsonObject attempt = new JsonObject();
            attempt.addProperty("modelo", model);
            attempt.addProperty("status", status);
            attempt.addProperty("mensaje", modelResponse.has("Mensaje")
                    ? modelResponse.get("Mensaje").getAsString()
                    : "Error al comunicarse con la IA");
            if (modelResponse.has("detalle")) {
                attempt.addProperty("detalle", modelResponse.get("detalle").getAsString());
            }
            attempts.add(attempt);
            lastError = modelResponse;
        }

        JsonObject failure = lastError != null ? lastError.deepCopy() : new JsonObject();
        if (!failure.has("Mensaje")) {
            failure.addProperty("Mensaje", "No se obtuvo respuesta de la IA");
        }
        if (!failure.has("status")) {
            failure.addProperty("status", 502);
        }
        failure.add("modelosIntentados", attempts);
        return failure;
    }
}
