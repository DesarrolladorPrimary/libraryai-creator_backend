package com.libraryai.backend.middleware;

import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.util.JwtUtil;
import com.libraryai.backend.util.QueryParams;
import com.sun.net.httpserver.HttpHandler;

/**
 * Middleware de autenticacion y roles.
 */
public class AuthMiddleware {
    /**
     * Envuelve un HttpHandler con validacion de JWT y control de roles.
     * Permite acceso si:
     * - el token es valido y el rol esta en rolesPermitidos, o
     * - el token corresponde al mismo id solicitado en la query.
     */
    public HttpHandler proteger( HttpHandler proximoPaso, String...rolesPermitidos) {
        return (exchange) -> {
            try {
                // Extrae el token de la cabecera Authorization.
                String peticion = exchange.getRequestHeaders().getFirst("Authorization");

                if (peticion == null || peticion.isEmpty()) {
                    ApiResponse.error(exchange, 401, "No tienes autorizacion para esto");
                    return;
                }
                // Espera el formato "Bearer <token>" y toma el token
                String[] parts = peticion.split(" ");
                String token = parts.length > 1 ? parts[1].trim() : parts[0].trim();

                // Valida el token y obtiene claims basicos (rol, usuario, id).
                JsonObject infoToken = JwtUtil.validateToken(token);

                if (infoToken.has("Mensaje")) {
                    ApiResponse.error(exchange, 401, infoToken.get("Mensaje").getAsString());
                    return;
                }
                String rolToken = infoToken.get("Rol").getAsString();
                int idToken = infoToken.get("Id").getAsInt();

                // Si la ruta incluye un id en query, permitimos acceso al mismo usuario.
                String query = exchange.getRequestURI().getQuery();
                int idSolicitado = -1;
                if (query != null && query.contains("id=")) {
                    idSolicitado = QueryParams.parseId(query).get("id").getAsInt();
                }

                if (idSolicitado != -1 && idToken == idSolicitado) {
                    proximoPaso.handle(exchange);
                    return;
                }

                // Verifica si el rol del token coincide con alguno de los roles permitidos.
                for (String unRol : rolesPermitidos) {
                    if (rolToken.equalsIgnoreCase(unRol)) {
                        proximoPaso.handle(exchange);
                        return;
                    }
                }
                // Si no coincide ningun rol ni id, se rechaza la peticion.
                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                
            } catch (Exception e) {
                // Fallback de error general del middleware.
                ApiResponse.error(exchange, 500, "Error por parte del servidor");
                e.printStackTrace();
            }
        };
    }

}
