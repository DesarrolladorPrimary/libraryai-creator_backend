package com.libraryai.backend.controller.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.auth.LoginService;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador HTTP del flujo de inicio de sesión.
 *
 * <p>Su responsabilidad es mínima: leer el cuerpo JSON, delegar en el servicio y
 * serializar la respuesta al cliente.
 */
public class LoginController {
    
    /**
     * Construye el handler de login.
     */
    public static HttpHandler loginUser(){
        return exchange -> {
            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

            // El controlador mantiene el parsing simple para dejar la lógica de
            // validación dentro del servicio.
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
