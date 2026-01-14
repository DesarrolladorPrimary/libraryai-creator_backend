package com.libraryai.backend.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import io.github.cdimascio.dotenv.Dotenv;

public class GeminiAI {

    private static final String API_KEY = Dotenv.load().get("GEMINI_API_KEY");

    public static String generarTexto(String responseUser, String instruccionesUser) {
        try (
            Client client = Client.builder().apiKey(API_KEY).build();
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
                "gemini-3-flash-preview", 
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
