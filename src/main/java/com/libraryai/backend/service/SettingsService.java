package com.libraryai.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.SettingsDao;
import com.libraryai.backend.dao.UserDao;
import com.libraryai.backend.util.JwtUtil;

/**
 * Lógica de negocio para configuración de usuario, suscripción y modelos IA.
 *
 * <p>Esta capa encapsula tres responsabilidades:
 * <p>1. Validar y persistir la instrucción permanente de Poly.
 * <p>2. Resolver el plan efectivo del usuario y su suscripción simulada.
 * <p>3. Traducir el plan activo a un catálogo de modelos y a un modelo IA preferente.
 */
public class SettingsService {

    private static final int MAX_INSTRUCCION_LENGTH = 1000;
    private static final String DEFAULT_PLAN = "Gratuito";

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
        String normalizedInstruction = instruccion == null ? "" : instruccion.trim();

        if (normalizedInstruction.length() > MAX_INSTRUCCION_LENGTH) {
            return buildErrorResponse(
                    400,
                    "La instrucción no puede superar " + MAX_INSTRUCCION_LENGTH + " caracteres");
        }

        JsonObject moderationResult = ModerationService.validateText(
                normalizedInstruction,
                userId,
                "La instrucción contiene contenido no permitido. Modifícala antes de guardarla.",
                "Instrucción permanente bloqueada por moderación");
        if (moderationResult != null) {
            return moderationResult;
        }

        return SettingsDao.updateInstruccionIA(userId, normalizedInstruction);
    }

    /**
     * Obtiene la suscripción activa del usuario.
     */
    public static JsonObject getSuscripcion(int userId) {
        return SettingsDao.getSuscripcionActiva(userId);
    }

    /**
     * Resuelve el nombre de plan efectivo con fallback seguro a Gratuito.
     */
    public static String getNormalizedPlanName(int userId) {
        JsonObject subscription = getSuscripcion(userId);

        if (!isOkResponse(subscription)) {
            return DEFAULT_PLAN;
        }

        return readPlanName(subscription);
    }

    /**
     * Indica si el usuario debe operar bajo reglas Premium.
     */
    public static boolean isPremiumUser(int userId) {
        return "Premium".equalsIgnoreCase(getNormalizedPlanName(userId));
    }

    /**
     * Simula el cambio de suscripción y devuelve un JWT actualizado con el nuevo rol.
     */
    public static JsonObject simulateSuscripcion(String plan, int userId) {
        if (plan == null || plan.trim().isEmpty()) {
            return buildErrorResponse(400, "Debes indicar el plan a simular");
        }

        JsonObject result = SettingsDao.simulateSubscriptionChange(userId, plan.trim());
        if (!isOkResponse(result)) {
            return result;
        }

        // El rol cambia en BD, pero el frontend sigue usando el JWT actual
        // hasta que reciba uno nuevo con el rol ya sincronizado.
        JsonObject user = UserDao.findById(userId);
        if (isOkResponse(user) && user.has("Correo") && user.has("Rol")) {
            String token = JwtUtil.generateUserToken(
                    user.get("Correo").getAsString(),
                    user.get("Rol").getAsString(),
                    userId);

            if (token != null && !token.isBlank()) {
                result.addProperty("Token", token);
            }
        }

        return result;
    }

    /**
     * Obtiene la versión actual del modelo IA y changelog (RF_23).
     */
    public static JsonObject getVersionIA(int userId) {
        JsonObject subscription = getRequiredSubscription(userId);
        if (!isOkResponse(subscription)) {
            return subscription;
        }

        return SettingsDao.getVersionActual(readPlanName(subscription));
    }

    /**
     * Obtiene modelos disponibles según plan del usuario (RF_23).
     */
    public static JsonObject getModeloDisponible(int userId) {
        JsonObject subscription = getRequiredSubscription(userId);
        if (!isOkResponse(subscription)) {
            return subscription;
        }

        return SettingsDao.getModelosPorPlan(readPlanName(subscription));
    }

    /**
     * Resuelve el modelo que Poly debe usar para el usuario.
     * Si el modelo solicitado no está disponible y failIfUnavailable es false,
     * cae al modelo activo permitido por plan.
     */
    public static JsonObject resolveModeloIA(int userId, Integer requestedModelId, boolean failIfUnavailable) {
        JsonObject subscription = getRequiredSubscription(userId);
        if (!isOkResponse(subscription)) {
            return subscription;
        }

        String plan = readPlanName(subscription);
        JsonObject availableModels = getModeloDisponible(userId);
        if (!isOkResponse(availableModels)) {
            return availableModels;
        }

        JsonArray models = extractModelsArray(availableModels);
        if (models.size() == 0) {
            return buildFallbackModelResponse(plan, models);
        }

        // Si el cliente pide un modelo concreto, primero intentamos respetarlo
        // y solo caemos al modelo por plan cuando el contrato lo permite.
        JsonObject selectedModel = requestedModelId != null
                ? findModelById(models, requestedModelId)
                : null;

        if (selectedModel == null && requestedModelId != null && failIfUnavailable) {
            return buildErrorResponse(403, "El modelo seleccionado no está disponible para tu plan");
        }

        if (selectedModel == null) {
            selectedModel = findCurrentPlanModel(plan, models);
        }

        if (selectedModel == null) {
            selectedModel = models.get(0).getAsJsonObject();
        }

        return buildSelectedModelResponse(selectedModel, models);
    }

    /**
     * Obtiene información completa del sistema (RF_23).
     */
    public static JsonObject getInfoSistema(int userId) {
        JsonObject versionInfo = getVersionIA(userId);
        JsonObject subscription = getRequiredSubscription(userId);

        if (!isOkResponse(versionInfo) || !isOkResponse(subscription)) {
            return buildErrorResponse(500, "Error al obtener información del sistema");
        }

        JsonObject response = new JsonObject();
        response.addProperty("status", 200);
        response.addProperty("versionModelo", versionInfo.get("version").getAsString());
        response.addProperty("nombreModelo", versionInfo.get("nombre").getAsString());
        response.addProperty("changelog", versionInfo.get("changelog").getAsString());
        response.addProperty("planUsuario", readPlanName(subscription));
        response.addProperty("modeloActivo", versionInfo.get("activo").getAsString());
        return response;
    }

    private static JsonObject getRequiredSubscription(int userId) {
        JsonObject subscription = SettingsDao.getSuscripcionActiva(userId);
        if (isOkResponse(subscription)) {
            return subscription;
        }

        // Centraliza el fallback para que los endpoints de settings respondan
        // con el mismo contrato cuando el usuario no tiene plan activo.
        return buildErrorResponse(404, "Usuario sin suscripción activa");
    }

    private static JsonArray extractModelsArray(JsonObject availableModels) {
        return availableModels.has("modelos") && availableModels.get("modelos").isJsonArray()
                ? availableModels.getAsJsonArray("modelos")
                : new JsonArray();
    }

    private static JsonObject buildFallbackModelResponse(String plan, JsonArray models) {
        // Este fallback evita dejar a Poly sin modelo si el catálogo filtrado
        // por plan viene vacío por seed incompleto o inconsistencia temporal.
        JsonObject currentModel = SettingsDao.getVersionActual(plan);
        if (isOkResponse(currentModel)) {
            return buildSelectedModelResponse(currentModel, models);
        }

        return buildErrorResponse(404, "No hay modelos disponibles para tu plan");
    }

    private static JsonObject findCurrentPlanModel(String plan, JsonArray models) {
        JsonObject currentModel = SettingsDao.getVersionActual(plan);
        if (!isOkResponse(currentModel)) {
            return null;
        }

        if (!currentModel.has("id")) {
            return currentModel;
        }

        return findModelById(models, currentModel.get("id").getAsInt());
    }

    private static JsonObject findModelById(JsonArray models, int modelId) {
        for (JsonElement item : models) {
            if (!item.isJsonObject()) {
                continue;
            }

            JsonObject model = item.getAsJsonObject();
            if (model.has("id") && model.get("id").getAsInt() == modelId) {
                return model;
            }
        }

        return null;
    }

    private static JsonObject buildSelectedModelResponse(JsonObject selectedModel, JsonArray models) {
        JsonObject response = new JsonObject();
        response.addProperty("status", 200);
        response.add("modelo", selectedModel);
        response.add("modelos", models);
        return response;
    }

    private static String readPlanName(JsonObject subscription) {
        String plan = subscription.has("plan") ? subscription.get("plan").getAsString() : DEFAULT_PLAN;
        return plan == null || plan.isBlank() ? DEFAULT_PLAN : plan.trim();
    }

    private static boolean isOkResponse(JsonObject json) {
        return json != null && json.has("status") && json.get("status").getAsInt() == 200;
    }

    private static JsonObject buildErrorResponse(int status, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", status);
        response.addProperty("Mensaje", message);
        return response;
    }
}
