package com.libraryai.backend.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.libraryai.backend.config.AIConfig;

public class GeminiAI {

    public static String generarTexto(String responseUser, String instruccionesUser) {
        try (
            Client client = Client.builder().apiKey(AIConfig.API_KEY).build();
        ){

            Content system = 
                Content.builder()
                    .role("system")
                    .parts(Part.fromText(instruccionesUser))
                    .build();


            GenerateContentConfig cfg =
                GenerateContentConfig.builder()
                    .systemInstruction(system)
                    .build();


            GenerateContentResponse resp = 
            client.models.generateContent(
                AIConfig.MODEL_AI, 
                responseUser, 
                cfg
            );
            
            System.out.println(resp.text());
            return resp.text();


        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return null;
        }


    }

}
