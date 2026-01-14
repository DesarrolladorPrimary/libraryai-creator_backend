package com.libraryai.backend.controller.ai;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.ai.GeminiService;
import com.sun.net.httpserver.HttpHandler;

public class AiController {

    public static HttpHandler generarHistoria() {
        return exchange -> {
            try {
                System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");
                ApiRequest request = new ApiRequest(exchange);
                String body = request.readBody();

                Gson gson = new Gson();
                JsonObject json = gson.fromJson(body, JsonObject.class);

                String mensaje = "";
                String instrucciones = "";

                if (json.has("mensaje")) {
                    mensaje = json.get("mensaje").getAsString();
                }

                if (json.has("instrucciones")) {
                    instrucciones = json.get("instrucciones").getAsString();
                }

                JsonObject responseJson = GeminiService.verificarGeneracionTexto(mensaje, instrucciones);
                
                int code = responseJson.has("status") ? responseJson.get("status").getAsInt() : 200;
                if (code != 200 && code != 500) {
                    code = 404;
                }

                responseJson.remove("status");

                String response = responseJson.toString();
                ApiResponse.send(exchange, response, code);



            } catch (JsonIOException e) {
                e.printStackTrace();
                ApiResponse.error(exchange, 404, "Mensaje");
            }

        };
    }

}
