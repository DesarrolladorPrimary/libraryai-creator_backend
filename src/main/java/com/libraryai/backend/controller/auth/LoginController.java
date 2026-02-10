package com.libraryai.backend.controller.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.auth.LoginService;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handlers HTTP para login.
 */
public class LoginController {
    
    /**
     * Handler de login.
     * Lee correo y contrasena del body, valida credenciales y responde con token.
     */
    public static HttpHandler loginUser(){
        return exchange -> {
            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

            // Parseo del JSON del body.
            Gson gson = new Gson();
            JsonObject user = gson.fromJson(body, JsonObject.class);

            String correo = "";
            String contraseña = "";

            if (user.has("correo")) {
                correo = user.get("correo").getAsString();
            }

            if (user.has("contraseña")) {
                contraseña = user.get("contraseña").getAsString();
            }

            JsonObject response = LoginService.validateLoginData(correo, contraseña);
            int code = response.get("status").getAsInt();
            response.remove("status");

            String responseJson = response.toString();


            ApiResponse.send(exchange, responseJson, code);
        };
    }
}
