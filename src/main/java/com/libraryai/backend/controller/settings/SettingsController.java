package com.libraryai.backend.controller.settings;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.settings.SettingsService;
import com.libraryai.backend.util.JwtUtil;
import com.libraryai.backend.util.QueryParams;
import com.sun.net.httpserver.HttpHandler;

/**
 * SETTINGSCONTROLLER - Controlador de configuraciones del usuario
 *
 * Rutas:
 * - GET /api/v1/settings/instruccion-ia?id=X  → obtiene instrucción permanente de Poly
 * - PUT /api/v1/settings/instruccion-ia?id=X  → actualiza instrucción permanente de Poly
 * - GET /api/v1/settings/suscripcion?id=X     → obtiene suscripción activa + datos del plan
 * - GET /api/v1/settings/planes?id=X          → obtiene catálogo visible de planes para el usuario
 * - PUT /api/v1/settings/suscripcion/simular?id=X → simula cambio de plan del usuario
 * - GET /api/v1/settings/version-ia?id=X      → obtiene versión actual del modelo IA (RF_23)
 * - GET /api/v1/settings/modelo-disponible?id=X → obtiene modelos disponibles según plan (RF_23)
 * - GET /api/v1/settings/sistema?id=X        → obtiene información completa del sistema (RF_23)
 */
public class SettingsController {

    /**
     * GET /api/v1/settings/instruccion-ia?id=X
     * Devuelve la instrucción permanente de IA guardada en Usuario.InstruccionPermanenteIA
     */
    public static HttpHandler getInstruccionIA() {
        return exchange -> {
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            String parametros = exchange.getRequestURI().getQuery();

            if (parametros == null || parametros.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(parametros);
            int code = idJson.get("status").getAsInt();

            if (code != 200) {
                ApiResponse.error(exchange, code, idJson.get("Mensaje").getAsString());
                return;
            }

            int id = idJson.get("id").getAsInt();
            if (!hasUserAccess(exchange.getRequestHeaders().getFirst("Authorization"), id)) {
                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                return;
            }
            JsonObject response = SettingsService.getInstruccionIA(id);

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * PUT /api/v1/settings/instruccion-ia?id=X
     * Body esperado: { "instruccion": "Escribe siempre en tono épico..." }
     * Actualiza Usuario.InstruccionPermanenteIA
     */
    public static HttpHandler updateInstruccionIA() {
        return exchange -> {
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            if (body.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la petición está vacío");
                return;
            }

            String parametros = exchange.getRequestURI().getQuery();

            if (parametros == null || parametros.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(parametros);
            int code = idJson.get("status").getAsInt();

            if (code != 200) {
                ApiResponse.error(exchange, code, idJson.get("Mensaje").getAsString());
                return;
            }

            int id = idJson.get("id").getAsInt();
            if (!hasUserAccess(exchange.getRequestHeaders().getFirst("Authorization"), id)) {
                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                return;
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);
            String instruccion = json.has("instruccion")
                ? json.get("instruccion").getAsString() : "";

            JsonObject response = SettingsService.updateInstruccionIA(instruccion, id);

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * GET /api/v1/settings/suscripcion?id=X
     * Devuelve la suscripción activa del usuario con datos del plan
     * (Suscripcion JOIN Plan)
     */
    public static HttpHandler getSuscripcion() {
        return exchange -> {
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            String parametros = exchange.getRequestURI().getQuery();

            if (parametros == null || parametros.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(parametros);
            int code = idJson.get("status").getAsInt();

            if (code != 200) {
                ApiResponse.error(exchange, code, idJson.get("Mensaje").getAsString());
                return;
            }

            int id = idJson.get("id").getAsInt();
            if (!hasUserAccess(exchange.getRequestHeaders().getFirst("Authorization"), id)) {
                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                return;
            }
            JsonObject response = SettingsService.getSuscripcion(id);

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * GET /api/v1/settings/planes?id=X
     * Devuelve el catálogo de planes visible para el usuario autenticado junto con
     * la suscripción actual para que el frontend renderice la comparación completa.
     */
    public static HttpHandler getPlanesDisponibles() {
        return exchange -> {
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            String parametros = exchange.getRequestURI().getQuery();

            if (parametros == null || parametros.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(parametros);
            int code = idJson.get("status").getAsInt();

            if (code != 200) {
                ApiResponse.error(exchange, code, idJson.get("Mensaje").getAsString());
                return;
            }

            int id = idJson.get("id").getAsInt();
            if (!hasUserAccess(exchange.getRequestHeaders().getFirst("Authorization"), id)) {
                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                return;
            }

            JsonObject response = SettingsService.getPlanesDisponibles(id);

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * PUT /api/v1/settings/suscripcion/simular?id=X
     * Body esperado: { "planId": 3 } o { "plan": "PREMIUM" }
     * Simula el cambio de plan sin pasarela de pago real.
     */
    public static HttpHandler simulateSuscripcion() {
        return exchange -> {
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            if (body == null || body.isBlank()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la petición está vacío");
                return;
            }

            String parametros = exchange.getRequestURI().getQuery();
            if (parametros == null || parametros.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(parametros);
            int code = idJson.get("status").getAsInt();

            if (code != 200) {
                ApiResponse.error(exchange, code, idJson.get("Mensaje").getAsString());
                return;
            }

            int id = idJson.get("id").getAsInt();
            if (!hasUserAccess(exchange.getRequestHeaders().getFirst("Authorization"), id)) {
                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                return;
            }

            JsonObject json;
            try {
                json = new Gson().fromJson(body, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo JSON es inválido");
                return;
            }

            String plan = json != null && json.has("plan") && !json.get("plan").isJsonNull()
                    ? json.get("plan").getAsString()
                    : "";
            Integer planId = json != null && json.has("planId") && !json.get("planId").isJsonNull()
                    ? json.get("planId").getAsInt()
                    : null;
            JsonObject response = SettingsService.simulateSuscripcion(planId, plan, id);

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * GET /api/v1/settings/version-ia?id=X
     * Retorna la versión actual del modelo IA y changelog (RF_23)
     */
    public static HttpHandler getVersionIA() {
        return exchange -> {
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            String parametros = exchange.getRequestURI().getQuery();
            if (parametros == null || parametros.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(parametros);
            if (idJson.get("status").getAsInt() != 200) {
                ApiResponse.error(exchange, 400, "ID inválido");
                return;
            }

            int id = idJson.get("id").getAsInt();
            if (!hasUserAccess(exchange.getRequestHeaders().getFirst("Authorization"), id)) {
                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                return;
            }
            JsonObject response = SettingsService.getVersionIA(id);

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * GET /api/v1/settings/modelo-disponible?id=X
     * Retorna modelos disponibles según plan del usuario (RF_23)
     */
    public static HttpHandler getModeloDisponible() {
        return exchange -> {
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            String parametros = exchange.getRequestURI().getQuery();
            if (parametros == null || parametros.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(parametros);
            if (idJson.get("status").getAsInt() != 200) {
                ApiResponse.error(exchange, 400, "ID inválido");
                return;
            }

            int id = idJson.get("id").getAsInt();
            if (!hasUserAccess(exchange.getRequestHeaders().getFirst("Authorization"), id)) {
                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                return;
            }
            JsonObject response = SettingsService.getModeloDisponible(id);

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    /**
     * GET /api/v1/settings/sistema?id=X
     * Retorna información completa del sistema (RF_32)
     */
    public static HttpHandler getInfoSistema() {
        return exchange -> {
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            String parametros = exchange.getRequestURI().getQuery();
            if (parametros == null || parametros.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(parametros);
            if (idJson.get("status").getAsInt() != 200) {
                ApiResponse.error(exchange, 400, "ID inválido");
                return;
            }

            int id = idJson.get("id").getAsInt();
            if (!hasUserAccess(exchange.getRequestHeaders().getFirst("Authorization"), id)) {
                ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                return;
            }
            JsonObject response = SettingsService.getInfoSistema(id);

            int statusCode = response.get("status").getAsInt();
            response.remove("status");
            ApiResponse.send(exchange, response.toString(), statusCode);
        };
    }

    private static boolean hasUserAccess(String authorizationHeader, int requestedUserId) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        JsonObject tokenInfo = JwtUtil.validateToken(token);

        if (tokenInfo.has("Mensaje")) {
            return false;
        }

        int tokenUserId = tokenInfo.get("Id").getAsInt();
        String role = tokenInfo.get("Rol").getAsString();

        return tokenUserId == requestedUserId || role.equalsIgnoreCase("Admin");
    }
}
