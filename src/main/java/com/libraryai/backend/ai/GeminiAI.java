package com.libraryai.backend.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.libraryai.backend.config.AIConfig;

/**
 * Cliente para generar contenido con Gemini.
 */
public class GeminiAI {

    /**
     * Llama al SDK de Gemini para generar texto a partir de un prompt.
     */
    public static String generarTexto(String responseUser, String instruccionesUser) {
        try (
            // Crea el cliente con la API key configurada.
            Client client = Client.builder().apiKey(AIConfig.API_KEY).build();
        ){

            // Instrucciones del sistema (contexto del modelo).
            Content system = 
                Content.builder()
                    .role("system")
                    .parts(Part.fromText(instruccionesUser))
                    .build();


            // Configuracion con instrucciones del sistema.
            GenerateContentConfig cfg =
                GenerateContentConfig.builder()
                    .systemInstruction(system)
                    .build();


            // Llama al modelo con prompt y configuracion.
            GenerateContentResponse resp = 
            client.models.generateContent(
                AIConfig.MODEL_AI, 
                responseUser, 
                cfg
            );
            
            // Log de la respuesta para debugging local.
            System.out.println(resp.text());
            return resp.text();


        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return null;
        }


    }

}
