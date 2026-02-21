package com.libraryai.backend.controller.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.auth.RecuperacionService;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador para recuperación de contraseña.
 */
public class RecuperacionController {

    /**
     * Handler para solicitar recuperación de contraseña.
     * Ruta: POST /api/v1/recuperar
     * Body esperado: { "correo": "usuario@correo.com" }
     */
    public static HttpHandler solicitarRecuperacion() {
        return exchange -> {
            // Lee el body de la peticion
            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            // Parsea el JSON recibido
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);

            // Extrae el correo del JSON
            String correo = "";
            if (json.has("correo")) {
                correo = json.get("correo").getAsString();
            }

            // Llama al service para procesar la solicitud
            JsonObject response = RecuperacionService.solicitarRecuperacion(correo);

            // Obtiene el status de la respuesta
            int status = response.get("status").getAsInt();
            // Remueve el status del JSON para no enviarlo al cliente
            response.remove("status");

            // Envia la respuesta al cliente
            ApiResponse.send(exchange, response.toString(), status);
        };
    }

    /**
     * Handler para validar token de recuperación.
     * Ruta: GET /api/v1/recuperar/validar?token=xxx
     */
    public static HttpHandler validarToken() {
        return exchange -> {
            // Obtiene el query string de la URL
            String query = exchange.getRequestURI().getQuery();
            String token = "";

            // Extrae el token del query
            if (query != null && query.contains("token=")) {
                token = query.split("token=")[1];
            }

            // Valida el token mediante el service
            JsonObject response = RecuperacionService.validarToken(token);

            // Obtiene el status de la respuesta
            int status = response.get("status").getAsInt();
            // Remueve el status del JSON
            response.remove("status");

            // Envia la respuesta
            ApiResponse.send(exchange, response.toString(), status);
        };
    }

    /**
     * Handler para establecer nueva contraseña.
     * Ruta: PUT /api/v1/recuperar/nueva
     * Body esperado: { "token": "xxx", "contraseña": "nueva123" }
     */
    public static HttpHandler nuevaPassword() {
        return exchange -> {
            // Lee el body de la peticion
            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            // Parsea el JSON
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);

            // Extrae token y nueva contraseña
            String token = "";
            String contraseña = "";

            if (json.has("token")) {
                token = json.get("token").getAsString();
            }
            if (json.has("contraseña")) {
                contraseña = json.get("contraseña").getAsString();
            }

            // Llama al service para procesar el cambio de contraseña
            JsonObject response = RecuperacionService.nuevaPassword(token, contraseña);

            // Obtiene el status de la respuesta
            int status = response.get("status").getAsInt();
            // Remueve el status del JSON
            response.remove("status");

            // Envia la respuesta al cliente
            ApiResponse.send(exchange, response.toString(), status);
        };
    }
}
