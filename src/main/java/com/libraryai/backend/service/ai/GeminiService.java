package com.libraryai.backend.service.ai;

import com.google.gson.JsonObject;
import com.libraryai.backend.ai.GeminiAI;

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
        String systemInstructions = instrucciones != null ? instrucciones.trim() : "";
        
        // No se puede generar respuesta sin prompt.
        if (prompt.isEmpty()) {
            responseAI.addProperty("Mensaje", "El mensaje esta vacio");
            responseAI.addProperty("status", 400);
            return responseAI;
        }

        // Llama al cliente de IA con el prompt e instrucciones.
        String response = GeminiAI.generateText(prompt, systemInstructions);
        // Si la IA no responde, devolvemos error.
        if (response == null || response.isBlank()) {
            responseAI.addProperty("Mensaje", "No se obtuvo respuesta de la IA");
            responseAI.addProperty("status", 500);
            return responseAI;
        }
        responseAI.addProperty("AI", response);
        responseAI.addProperty("status", 200);



        return responseAI;
    }
}
