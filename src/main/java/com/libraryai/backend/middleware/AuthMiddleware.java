package com.libraryai.backend.middleware;

import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.util.JwtUtil;
import com.sun.net.httpserver.HttpHandler;

public class AuthMiddleware {
    public HttpHandler proteger(String rolRequerido, HttpHandler proximoPaso) {
        return (exchange) -> {
           try {
             String peticion = exchange.getRequestHeaders().getFirst("Authorization");

            if (peticion == null || peticion.isEmpty()) {
                ApiResponse.error(exchange, 401, "No tienes autorizacion para esto");
                return;
            }
            String[] parts = peticion.split("Bearer");
            String rolUser = parts[parts.length - 1].trim();

            JsonObject infoToken = JwtUtil.validarToken(rolUser);

            if (infoToken.has("Mensaje")) {
                ApiResponse.error(exchange, 401, infoToken.get("Mensaje").getAsString());
                return;
            }
            String very = infoToken.get("Rol").getAsString();

            if (!very.equals(rolRequerido)) {
                ApiResponse.error(exchange, 403, "No tiene permisos de ADMIN");
                return;
            }
            System.out.println(very);
            proximoPaso.handle(exchange);
           } catch (Exception e) {
                ApiResponse.error(exchange, 500, "Error por parte del servidor");
                e.printStackTrace();
           }
        };
    }

}
