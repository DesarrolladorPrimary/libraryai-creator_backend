package com.libraryai.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.AdminDao;

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
}
