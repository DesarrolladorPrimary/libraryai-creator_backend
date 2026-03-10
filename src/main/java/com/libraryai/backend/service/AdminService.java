package com.libraryai.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.AdminDao;

/**
 * Lógica de negocio del panel de administración.
 */
public class AdminService {

    public static JsonArray listAdminUsers() {
        return AdminDao.listAdminUsers();
    }

    public static JsonObject updateUserStatus(int userId, boolean active) {
        if (userId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "ID de usuario inválido");
            response.addProperty("status", 400);
            return response;
        }

        return AdminDao.updateUserStatus(userId, active);
    }

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

    public static JsonObject getSystemStats() {
        return AdminDao.getSystemStats();
    }

    public static JsonArray getPlansSummary() {
        return AdminDao.getPlansSummary();
    }

    public static JsonArray getPaymentHistory() {
        return AdminDao.getPaymentHistory();
    }
}
