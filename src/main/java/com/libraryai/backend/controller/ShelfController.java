package com.libraryai.backend.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.ShelfService;
import com.libraryai.backend.util.QueryParams;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador para gestionar estanterías.
 */
public class ShelfController {

    /**
     * GET /api/v1/estanterias?id=X
     * Obtiene todas las estanterías del usuario.
     */
    public static HttpHandler listShelves() {
        return exchange -> {
            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            String query = exchange.getRequestURI().getQuery();
            if (query == null || query.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(query);
            if (idJson.get("status").getAsInt() != 200) {
                ApiResponse.error(exchange, 400, "ID inválido");
                return;
            }
            int usuarioId = idJson.get("id").getAsInt();

            JsonArray response = ShelfService.obtenerEstanterias(usuarioId);
            ApiResponse.send(exchange, response.toString(), 200);
        };
    }

    /**
     * POST /api/v1/estanterias
     * Body: { "usuarioId": X, "nombre": "Mi estantería" }
     */
    public static HttpHandler createShelf() {
        return exchange -> {
            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            if (body.isEmpty()) {
                ApiResponse.error(exchange, 400, "No hay cuerpo en la petición");
                return;
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);

            int usuarioId = json.get("usuarioId").getAsInt();
            String nombre = json.get("nombre").getAsString();

            JsonObject response = ShelfService.crearEstanteria(usuarioId, nombre);

            int status = response.has("Status") ? response.get("Status").getAsInt() : 200;
            response.remove("Status");

            ApiResponse.send(exchange, response.toString(), status);
        };
    }

    /**
     * PUT /api/v1/estanterias?id=X
     * Body: { "nombre": "Nuevo nombre" }
     */
    public static HttpHandler updateShelf() {
        return exchange -> {
            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            String query = exchange.getRequestURI().getQuery();
            if (query == null || query.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID de la estantería");
                return;
            }

            JsonObject idJson = QueryParams.parseId(query);
            if (idJson.get("status").getAsInt() != 200) {
                ApiResponse.error(exchange, 400, "ID inválido");
                return;
            }
            int estanteriaId = idJson.get("id").getAsInt();

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            if (body.isEmpty()) {
                ApiResponse.error(exchange, 400, "No hay cuerpo en la petición");
                return;
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);
            String nombre = json.get("nombre").getAsString();

            JsonObject response = ShelfService.actualizarEstanteria(nombre, estanteriaId);

            int status = response.has("Status") ? response.get("Status").getAsInt() : 200;
            response.remove("Status");

            ApiResponse.send(exchange, response.toString(), status);
        };
    }

    /**
     * DELETE /api/v1/estanterias?id=X
     */
    public static HttpHandler deleteShelf() {
        return exchange -> {
            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            String query = exchange.getRequestURI().getQuery();
            if (query == null || query.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID de la estantería");
                return;
            }

            JsonObject idJson = QueryParams.parseId(query);
            if (idJson.get("status").getAsInt() != 200) {
                ApiResponse.error(exchange, 400, "ID inválido");
                return;
            }
            int estanteriaId = idJson.get("id").getAsInt();

            JsonObject response = ShelfService.eliminarEstanteria(estanteriaId);

            int status = response.has("Status") ? response.get("Status").getAsInt() : 200;
            response.remove("Status");

            ApiResponse.send(exchange, response.toString(), status);
        };
    }
}
