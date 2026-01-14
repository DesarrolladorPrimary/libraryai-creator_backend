package com.libraryai.backend.service.ai;

import com.google.gson.JsonObject;
import com.libraryai.backend.ai.GeminiAI;

public class GeminiService {
    
    public static JsonObject verificarGeneracionTexto(String mensaje, String instrucciones){
        JsonObject responseAI = new JsonObject();
        
        if (mensaje.isEmpty()) {
            responseAI.addProperty("mensaje", "El mensaje esta vacio"); 
            responseAI.addProperty("status", 404);
            return responseAI;
        }

        String response = GeminiAI.generarTexto(mensaje, instrucciones);
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
