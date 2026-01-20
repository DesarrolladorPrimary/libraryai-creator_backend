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
    public static JsonObject verificarGeneracionTexto(String mensaje, String instrucciones){
        JsonObject responseAI = new JsonObject();
        
        // No se puede generar respuesta sin prompt.
        if (mensaje.isEmpty()) {
            responseAI.addProperty("mensaje", "El mensaje esta vacio"); 
            responseAI.addProperty("status", 404);
            return responseAI;
        }

        // Llama al cliente de IA con el prompt e instrucciones.
        String response = GeminiAI.generarTexto(mensaje, instrucciones);
        // Si la IA no responde, devolvemos error.
        if (response == null || response.isBlank()) {
            responseAI.addProperty("mensaje", "No se obtuvo respuesta de la IA");
            responseAI.addProperty("status", 500);
            return responseAI;
        }
        responseAI.addProperty("AI", response);
        responseAI.addProperty("status", 200);



        return responseAI;
    }
}
