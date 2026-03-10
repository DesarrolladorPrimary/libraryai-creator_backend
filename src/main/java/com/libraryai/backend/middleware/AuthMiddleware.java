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
     * - el token corresponde al mismo id solicitado en la query cuando la ruta no es exclusiva de Admin.
     */
    public HttpHandler proteger(HttpHandler proximoPaso, String... rolesPermitidos) {
        return (exchange) -> {
            try {
                String peticion = exchange.getRequestHeaders().getFirst("Authorization");

                if (peticion == null || peticion.isEmpty()) {
                    JsonObject response = new JsonObject();
                    response.addProperty("Mensaje", "Debes iniciar sesion para continuar.");
                    response.addProperty("status", 401);
                    response.addProperty("code", "TOKEN_MISSING");
                    ApiResponse.send(exchange, response.toString(), 401);
                    return;
                }

                String[] parts = peticion.split(" ");
                String token = parts.length > 1 ? parts[1].trim() : parts[0].trim();

                JsonObject infoToken = JwtUtil.validateToken(token);

                if (infoToken.has("Mensaje")) {
                    int status = infoToken.has("status") ? infoToken.get("status").getAsInt() : 401;
                    ApiResponse.send(exchange, infoToken.toString(), status);
                    return;
                }
                String rolToken = infoToken.get("Rol").getAsString();
                int idToken = infoToken.get("Id").getAsInt();

                String query = exchange.getRequestURI().getQuery();
                int idSolicitado = -1;
                if (query != null && query.contains("id=")) {
                    JsonObject parsedId = QueryParams.parseId(query);
                    if (parsedId.get("status").getAsInt() == 200) {
                        idSolicitado = parsedId.get("id").getAsInt();
                    }
                }

                if (!isAdminOnlyRoute(rolesPermitidos) && idSolicitado != -1 && idToken == idSolicitado) {
                    proximoPaso.handle(exchange);
                    return;
                }

                for (String unRol : rolesPermitidos) {
                    if (rolToken.equalsIgnoreCase(unRol)) {
                        proximoPaso.handle(exchange);
                        return;
                    }
                }

                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");

            } catch (Exception e) {
                ApiResponse.error(exchange, 500, "Error por parte del servidor");
                e.printStackTrace();
            }
        };
    }

    private boolean isAdminOnlyRoute(String... rolesPermitidos) {
        return rolesPermitidos != null
                && rolesPermitidos.length == 1
                && "Admin".equalsIgnoreCase(rolesPermitidos[0]);
    }
}
