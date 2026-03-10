package com.libraryai.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

        JsonObject moderationResult = ModerationService.validateText(
                instruccion,
                userId,
                "La instrucción contiene contenido no permitido. Modifícala antes de guardarla.",
                "Instrucción permanente bloqueada por moderación");
        if (moderationResult != null) {
            return moderationResult;
        }

        return SettingsDao.updateInstruccionIA(userId, instruccion);
    }

    /**
     * Obtiene la suscripción activa del usuario.
     */
    public static JsonObject getSuscripcion(int userId) {
        return SettingsDao.getSuscripcionActiva(userId);
    }

    /**
     * Obtiene la versión actual del modelo IA y changelog (RF_32).
     */
    public static JsonObject getVersionIA(int userId) {
        return SettingsDao.getVersionActual();
    }

    /**
     * Obtiene modelos disponibles según plan del usuario (RF_32).
     */
    public static JsonObject getModeloDisponible(int userId) {
        // Primero obtener el plan del usuario
        JsonObject suscripcion = SettingsDao.getSuscripcionActiva(userId);
        
        if (suscripcion.get("status").getAsInt() != 200) {
            JsonObject response = new JsonObject();
            response.addProperty("status", 404);
            response.addProperty("Mensaje", "Usuario sin suscripción activa");
            return response;
        }
        
        String plan = suscripcion.get("plan").getAsString();
        return SettingsDao.getModelosPorPlan(plan);
    }

    /**
     * Resuelve el modelo que Poly debe usar para el usuario.
     * Si el modelo solicitado no está disponible y failIfUnavailable es false,
     * cae al modelo activo permitido por plan.
     */
    public static JsonObject resolveModeloIA(int userId, Integer requestedModelId, boolean failIfUnavailable) {
        JsonObject availableModels = getModeloDisponible(userId);
        if (!availableModels.has("status") || availableModels.get("status").getAsInt() != 200) {
            return availableModels;
        }

        JsonArray modelos = availableModels.has("modelos") && availableModels.get("modelos").isJsonArray()
                ? availableModels.getAsJsonArray("modelos")
                : new JsonArray();

        if (modelos.size() == 0) {
            JsonObject currentModel = SettingsDao.getVersionActual();
            if (currentModel.has("status") && currentModel.get("status").getAsInt() == 200) {
                JsonObject response = new JsonObject();
                response.addProperty("status", 200);
                response.add("modelo", currentModel);
                response.add("modelos", modelos);
                return response;
            }

            JsonObject response = new JsonObject();
            response.addProperty("status", 404);
            response.addProperty("Mensaje", "No hay modelos disponibles para tu plan");
            return response;
        }

        JsonObject selectedModel = null;

        if (requestedModelId != null) {
            for (JsonElement item : modelos) {
                if (!item.isJsonObject()) {
                    continue;
                }

                JsonObject modelo = item.getAsJsonObject();
                if (modelo.has("id") && modelo.get("id").getAsInt() == requestedModelId) {
                    selectedModel = modelo;
                    break;
                }
            }

            if (selectedModel == null && failIfUnavailable) {
                JsonObject response = new JsonObject();
                response.addProperty("status", 403);
                response.addProperty("Mensaje", "El modelo seleccionado no está disponible para tu plan");
                return response;
            }
        }

        if (selectedModel == null) {
            JsonObject currentModel = SettingsDao.getVersionActual();
            if (currentModel.has("status") && currentModel.get("status").getAsInt() == 200) {
                if (currentModel.has("id")) {
                    int currentModelId = currentModel.get("id").getAsInt();
                    for (JsonElement item : modelos) {
                        if (!item.isJsonObject()) {
                            continue;
                        }

                        JsonObject modelo = item.getAsJsonObject();
                        if (modelo.has("id") && modelo.get("id").getAsInt() == currentModelId) {
                            selectedModel = modelo;
                            break;
                        }
                    }
                } else {
                    selectedModel = currentModel;
                }
            }
        }

        if (selectedModel == null) {
            selectedModel = modelos.get(0).getAsJsonObject();
        }

        JsonObject response = new JsonObject();
        response.addProperty("status", 200);
        response.add("modelo", selectedModel);
        response.add("modelos", modelos);
        return response;
    }

    /**
     * Obtiene información completa del sistema (RF_32).
     */
    public static JsonObject getInfoSistema(int userId) {
        JsonObject response = new JsonObject();
        
        // Obtener versión actual del modelo
        JsonObject versionInfo = SettingsDao.getVersionActual();
        
        // Obtener suscripción del usuario
        JsonObject suscripcion = SettingsDao.getSuscripcionActiva(userId);
        
        if (versionInfo.get("status").getAsInt() == 200 && suscripcion.get("status").getAsInt() == 200) {
            response.addProperty("status", 200);
            response.addProperty("versionModelo", versionInfo.get("version").getAsString());
            response.addProperty("nombreModelo", versionInfo.get("nombre").getAsString());
            response.addProperty("changelog", versionInfo.get("changelog").getAsString());
            response.addProperty("planUsuario", suscripcion.get("plan").getAsString());
            response.addProperty("modeloActivo", versionInfo.get("activo").getAsString());
        } else {
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "Error al obtener información del sistema");
        }
        
        return response;
    }
}
