package com.libraryai.backend.service;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.SettingsDao;

/**
 * SETTINGSSERVICE - Lógica de negocio para configuraciones
 *
 * Valida los datos antes de pasarlos al DAO.
 */
public class SettingsService {

    private static final int MAX_INSTRUCCION_LENGTH = 1000;

    /**
     * Obtiene la instrucción permanente de IA del usuario.
     */
    public static JsonObject getInstruccionIA(int userId) {
        return SettingsDao.getInstruccionIA(userId);
    }

    /**
     * Valida y actualiza la instrucción permanente de IA.
     * Máximo: 1000 caracteres.
     */
    public static JsonObject updateInstruccionIA(String instruccion, int userId) {
        JsonObject response = new JsonObject();

        if (instruccion == null) instruccion = "";
        instruccion = instruccion.trim();

        if (instruccion.length() > MAX_INSTRUCCION_LENGTH) {
            response.addProperty("status", 400);
            response.addProperty("Mensaje",
                "La instrucción no puede superar " + MAX_INSTRUCCION_LENGTH + " caracteres");
            return response;
        }

        return SettingsDao.updateInstruccionIA(userId, instruccion);
    }

    /**
     * Obtiene la suscripción activa del usuario.
     */
    public static JsonObject getSuscripcion(int userId) {
        return SettingsDao.getSuscripcionActiva(userId);
    }
}
