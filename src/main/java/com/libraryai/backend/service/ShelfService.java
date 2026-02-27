package com.libraryai.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.shelves.ShelfDao;

/**
 * Servicio para gestionar estanterías.
 */
public class ShelfService {

    /**
     * Crea una nueva estantería para el usuario.
     */
    public static JsonObject crearEstanteria(int usuarioId, String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El nombre no puede estar vacío");
            response.addProperty("status", 400);
            return response;
        }

        return ShelfDao.createShelf(usuarioId, nombre.trim());
    }

    /**
     * Obtiene todas las estanterías del usuario.
     */
    public static JsonArray obtenerEstanterias(int usuarioId) {
        return ShelfDao.getShelvesByUserId(usuarioId);
    }

    /**
     * Actualiza el nombre de una estantería.
     */
    public static JsonObject actualizarEstanteria(String nombre, int estanteriaId) {
        if (nombre == null || nombre.trim().isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El nombre no puede estar vacío");
            response.addProperty("status", 400);
            return response;
        }

        return ShelfDao.updateShelf(nombre.trim(), estanteriaId);
    }

    /**
     * Elimina una estantería.
     */
    public static JsonObject eliminarEstanteria(int estanteriaId) {
        return ShelfDao.deleteShelf(estanteriaId);
    }
}
