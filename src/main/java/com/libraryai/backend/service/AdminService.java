package com.libraryai.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.AdminDao;
import com.libraryai.backend.dao.SettingsDao;

/**
 * Lógica de negocio del panel de administración.
 */
public class AdminService {

    /**
     * Obtiene la lista de usuarios gestionables en el panel admin.
     */
    public static JsonArray listAdminUsers() {
        return AdminDao.listAdminUsers();
    }

    /**
     * Actualiza el estado activo/suspendido de un usuario.
     */
    public static JsonObject updateUserStatus(int userId, boolean active) {
        if (userId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "ID de usuario inválido");
            response.addProperty("status", 400);
            return response;
        }

        return AdminDao.updateUserStatus(userId, active);
    }

    /**
     * Cambia el rol de un usuario validando datos mínimos del administrador.
     */
    public static JsonObject updateUserRole(int userId, int adminId, String newRole) {
        JsonObject response = new JsonObject();

        if (userId <= 0) {
            response.addProperty("Mensaje", "ID de usuario inválido");
            response.addProperty("status", 400);
            return response;
        }

        if (adminId <= 0) {
            response.addProperty("Mensaje", "No fue posible identificar al administrador");
            response.addProperty("status", 401);
            return response;
        }

        if (newRole == null || newRole.isBlank()) {
            response.addProperty("Mensaje", "Debe enviar un rol válido");
            response.addProperty("status", 400);
            return response;
        }

        return AdminDao.updateUserRole(userId, adminId, newRole.trim());
    }

    /**
     * Obtiene métricas globales del sistema.
     */
    public static JsonObject getSystemStats() {
        return AdminDao.getSystemStats();
    }

    /**
     * Obtiene el resumen de planes configurados.
     */
    public static JsonArray getPlansSummary() {
        return AdminDao.getPlansSummary();
    }

    /**
     * Obtiene el historial de pagos simulados.
     */
    public static JsonArray getPaymentHistory() {
        return AdminDao.getPaymentHistory();
    }

    /**
     * Obtiene el historial de eventos bloqueados por moderación.
     */
    public static JsonArray getModerationLogs() {
        return AdminDao.getModerationLogs();
    }

    /**
     * Devuelve el catálogo completo de modelos IA visible para administración.
     */
    public static JsonObject getModelCatalog() {
        JsonObject response = new JsonObject();

        JsonObject freeModel = SettingsDao.getVersionActual("Gratuito");
        if (!freeModel.has("status") || freeModel.get("status").getAsInt() != 200) {
            return freeModel;
        }

        JsonObject premiumModel = SettingsDao.getVersionActual("Premium");
        if (!premiumModel.has("status") || premiumModel.get("status").getAsInt() != 200) {
            return premiumModel;
        }

        JsonObject availableModels = SettingsDao.getModelosPorPlan("Premium");
        if (!availableModels.has("status") || availableModels.get("status").getAsInt() != 200) {
            return availableModels;
        }

        response.addProperty("status", 200);
        response.add("modeloGratuito", freeModel);
        response.add("modeloPremium", premiumModel);
        response.add("modelos", availableModels.get("modelos"));
        response.addProperty("total", availableModels.has("total") ? availableModels.get("total").getAsInt() : 0);
        return response;
    }
}
