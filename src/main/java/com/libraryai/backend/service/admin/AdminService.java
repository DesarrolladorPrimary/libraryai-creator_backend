package com.libraryai.backend.service.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.admin.AdminDao;
import com.libraryai.backend.dao.settings.SettingsDao;

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
     * Crea un plan nuevo dentro del catálogo administrable.
     */
    public static JsonObject createPlan(JsonObject payload) {
        return savePlan(0, payload, true);
    }

    /**
     * Actualiza un plan existente del catálogo administrable.
     */
    public static JsonObject updatePlan(int planId, JsonObject payload) {
        if (planId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "ID de plan inválido");
            response.addProperty("status", 400);
            return response;
        }

        return savePlan(planId, payload, false);
    }

    /**
     * Crea o reemplaza la suscripción activa de un usuario desde admin.
     */
    public static JsonObject updateUserSubscription(int userId, JsonObject payload) {
        JsonObject response = new JsonObject();

        if (userId <= 0) {
            response.addProperty("Mensaje", "ID de usuario inválido");
            response.addProperty("status", 400);
            return response;
        }

        boolean cancel = payload != null && payload.has("cancelar") && payload.get("cancelar").getAsBoolean();
        Integer planId = null;
        if (payload != null && payload.has("planId") && !payload.get("planId").isJsonNull()) {
            planId = payload.get("planId").getAsInt();
        }

        if (!cancel && (planId == null || planId <= 0)) {
            response.addProperty("Mensaje", "Debes seleccionar un plan o indicar que deseas cancelar la suscripción");
            response.addProperty("status", 400);
            return response;
        }

        return AdminDao.updateUserSubscription(userId, planId, cancel);
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

    private static JsonObject savePlan(int planId, JsonObject payload, boolean creating) {
        JsonObject response = new JsonObject();

        if (payload == null) {
            response.addProperty("Mensaje", "El cuerpo JSON es obligatorio");
            response.addProperty("status", 400);
            return response;
        }

        String code = payload.has("codigo") ? payload.get("codigo").getAsString().trim().toUpperCase() : "";
        String name = payload.has("nombre") ? payload.get("nombre").getAsString().trim() : "";
        String roleBase = payload.has("rolBase") ? payload.get("rolBase").getAsString().trim() : "";
        boolean active = !payload.has("activo") || payload.get("activo").getAsBoolean();
        Long storageMb = null;
        if (payload.has("almacenamientoMaxMB") && !payload.get("almacenamientoMaxMB").isJsonNull()) {
            storageMb = payload.get("almacenamientoMaxMB").getAsLong();
        }
        double price = payload.has("precio") ? payload.get("precio").getAsDouble() : 0;

        if (!code.matches("[A-Z0-9_]{2,30}")) {
            response.addProperty("Mensaje", "El código del plan debe tener entre 2 y 30 caracteres en mayúsculas, números o guion bajo");
            response.addProperty("status", 400);
            return response;
        }

        if (name.length() < 3 || name.length() > 100) {
            response.addProperty("Mensaje", "El nombre del plan debe tener entre 3 y 100 caracteres");
            response.addProperty("status", 400);
            return response;
        }

        if (!"Gratuito".equalsIgnoreCase(roleBase) && !"Premium".equalsIgnoreCase(roleBase)) {
            response.addProperty("Mensaje", "El rol base del plan debe ser Gratuito o Premium");
            response.addProperty("status", 400);
            return response;
        }

        if (storageMb != null && storageMb < 0) {
            response.addProperty("Mensaje", "El almacenamiento del plan no puede ser negativo");
            response.addProperty("status", 400);
            return response;
        }

        if (price < 0) {
            response.addProperty("Mensaje", "El precio del plan no puede ser negativo");
            response.addProperty("status", 400);
            return response;
        }

        if (creating) {
            return AdminDao.createPlan(code, name, storageMb, java.math.BigDecimal.valueOf(price), active, roleBase);
        }

        return AdminDao.updatePlan(planId, code, name, storageMb, java.math.BigDecimal.valueOf(price), active,
                roleBase);
    }
}
