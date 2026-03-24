package com.libraryai.backend.service.shelf;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.shelves.ShelfDao;

/**
 * Servicio para gestionar estanterías globales.
 */
public class ShelfService {

    /**
     * Crea una nueva estantería global.
     */
    public static JsonObject crearEstanteria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El nombre no puede estar vacío");
            response.addProperty("status", 400);
            return response;
        }

        return ShelfDao.createShelf(nombre.trim());
    }

    /**
     * Obtiene todas las estanterías globales.
     */
    public static JsonArray obtenerEstanterias() {
        return ShelfDao.getAllShelves();
    }

    /**
     * Valida si una estantería global existe.
     */
    public static boolean existeEstanteria(int estanteriaId) {
        return estanteriaId > 0 && ShelfDao.existsById(estanteriaId);
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
