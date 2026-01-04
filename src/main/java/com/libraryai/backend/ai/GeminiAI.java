package com.libraryai.backend.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiAI {

    public static void getGemini() {
        try (Client client = new Client()) {
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    "hola",
                    null);

            System.out.println(response.text());
        }
    }

}
