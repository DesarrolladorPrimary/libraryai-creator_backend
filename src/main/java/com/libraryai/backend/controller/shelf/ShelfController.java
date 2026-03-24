package com.libraryai.backend.controller.shelf;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.shelf.ShelfService;
import com.libraryai.backend.util.QueryParams;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador para gestionar estanterías.
 */
public class ShelfController {

    /**
     * GET /api/v1/estanterias
     * Obtiene todas las estanterías globales disponibles.
     */
    public static HttpHandler listShelves() {
        return exchange -> {
            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            JsonArray response = ShelfService.obtenerEstanterias();
            if (response.size() == 1 && response.get(0).isJsonObject()) {
                JsonObject maybeError = response.get(0).getAsJsonObject();
                if (maybeError.has("status") && maybeError.get("status").getAsInt() >= 400) {
                    int status = maybeError.get("status").getAsInt();
                    maybeError.remove("status");
                    ApiResponse.send(exchange, maybeError.toString(), status);
                    return;
                }
            }

            ApiResponse.send(exchange, response.toString(), 200);
        };
    }

    /**
     * POST /api/v1/estanterias
     * Body: { "nombre": "Mi estantería" }
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
            if (json == null || !json.has("nombre") || json.get("nombre").isJsonNull()) {
                ApiResponse.error(exchange, 400, "Debes enviar un nombre para la estantería");
                return;
            }

            String nombre = json.get("nombre").getAsString();
            JsonObject response = ShelfService.crearEstanteria(nombre);

            int status = response.has("status") ? response.get("status").getAsInt() : 200;
            response.remove("status");

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

            int status = response.has("status") ? response.get("status").getAsInt() : 200;
            response.remove("status");

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

            int status = response.has("status") ? response.get("status").getAsInt() : 200;
            response.remove("status");

            ApiResponse.send(exchange, response.toString(), status);
        };
    }
}
