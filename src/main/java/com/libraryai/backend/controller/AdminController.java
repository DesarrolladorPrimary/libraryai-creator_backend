package com.libraryai.backend.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.AdminService;
import com.libraryai.backend.util.JwtUtil;
import com.libraryai.backend.util.QueryParams;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador del módulo admin.
 */
public class AdminController {

    /**
     * Expone el listado de usuarios visible para administración.
     */
    public static HttpHandler listUsers() {
        return exchange -> {
            JsonArray response = AdminService.listAdminUsers();
            int statusCode = extractArrayStatus(response);
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * Suspende o reactiva un usuario según el flag recibido en el body.
     */
    public static HttpHandler updateUserStatus() {
        return exchange -> {
            String query = exchange.getRequestURI().getQuery();

            if (query == null || query.isBlank()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(query);
            int code = idJson.get("status").getAsInt();

            if (code != 200) {
                ApiResponse.error(exchange, code, idJson.get("Mensaje").getAsString());
                return;
            }

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            if (body.isBlank()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la petición está vacío");
                return;
            }

            Gson gson = new Gson();
            JsonObject payload = gson.fromJson(body, JsonObject.class);

            if (!payload.has("activo")) {
                ApiResponse.error(exchange, 400, "Debe enviar el campo activo");
                return;
            }

            JsonObject response = AdminService.updateUserStatus(
                    idJson.get("id").getAsInt(),
                    payload.get("activo").getAsBoolean());

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * Cambia el rol de un usuario y registra la auditoría del cambio.
     */
    public static HttpHandler updateUserRole() {
        return exchange -> {
            String query = exchange.getRequestURI().getQuery();

            if (query == null || query.isBlank()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(query);
            int code = idJson.get("status").getAsInt();

            if (code != 200) {
                ApiResponse.error(exchange, code, idJson.get("Mensaje").getAsString());
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            int adminId = extractAdminId(authHeader);
            if (adminId <= 0) {
                ApiResponse.error(exchange, 401, "No fue posible validar la sesión del administrador");
                return;
            }

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();
            if (body.isBlank()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la petición está vacío");
                return;
            }

            Gson gson = new Gson();
            JsonObject payload = gson.fromJson(body, JsonObject.class);
            if (payload == null || !payload.has("rol")) {
                ApiResponse.error(exchange, 400, "Debe enviar el campo rol");
                return;
            }

            JsonObject response = AdminService.updateUserRole(
                    idJson.get("id").getAsInt(),
                    adminId,
                    payload.get("rol").getAsString());

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * Devuelve métricas agregadas del sistema para el dashboard admin.
     */
    public static HttpHandler getStats() {
        return exchange -> {
            JsonObject response = AdminService.getSystemStats();
            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * Devuelve el resumen de planes disponibles y usuarios activos por plan.
     */
    public static HttpHandler getPlans() {
        return exchange -> {
            JsonArray response = AdminService.getPlansSummary();
            int statusCode = extractArrayStatus(response);
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * Devuelve el historial de pagos simulados visible para administración.
     */
    public static HttpHandler getPayments() {
        return exchange -> {
            JsonArray response = AdminService.getPaymentHistory();
            int statusCode = extractArrayStatus(response);
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * Devuelve el historial de contenido bloqueado por moderación.
     */
    public static HttpHandler getModerationLogs() {
        return exchange -> {
            JsonArray response = AdminService.getModerationLogs();
            int statusCode = extractArrayStatus(response);
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * Devuelve el catálogo completo de modelos IA disponible para administración.
     */
    public static HttpHandler getModels() {
        return exchange -> {
            JsonObject response = AdminService.getModelCatalog();
            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    private static int extractAdminId(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return -1;
        }

        String[] parts = authHeader.split(" ");
        String token = parts.length > 1 ? parts[1].trim() : parts[0].trim();
        JsonObject tokenData = JwtUtil.validateToken(token);

        if (tokenData.has("Mensaje") || !tokenData.has("Id")) {
            return -1;
        }

        return tokenData.get("Id").getAsInt();
    }

    private static int extractArrayStatus(JsonArray response) {
        if (response.size() == 0) {
            return 200;
        }

        JsonObject firstItem = response.get(0).getAsJsonObject();
        if (firstItem.has("status")) {
            return firstItem.get("status").getAsInt();
        }

        return 200;
    }
}
