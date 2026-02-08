package com.libraryai.backend.controller.ai;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.ai.GeminiService;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handlers HTTP para funciones de IA.
 */
public class AiController {

    /**
     * Handler para generar historias con IA.
     * Lee mensaje/instrucciones, delega al servicio y normaliza status.
     */
    public static HttpHandler generateStory() {
        return exchange -> {
            try {
                System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");
                // Lee el body de la peticion.
                ApiRequest request = new ApiRequest(exchange);
                String body = request.readBody();

                // Convierte el JSON del body a JsonObject.
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(body, JsonObject.class);

                String mensaje = "";
                String instrucciones = "";

                // Lee campos opcionales del payload.
                if (json.has("mensaje")) {
                    mensaje = json.get("mensaje").getAsString();
                }

                if (json.has("instrucciones")) {
                    instrucciones = json.get("instrucciones").getAsString();
                }

                // Ejecuta la generacion y obtiene respuesta.
                JsonObject responseJson = GeminiService.verificarGeneracionTexto(mensaje, instrucciones);
                
                // Usa status del servicio o 200 por defecto.
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
