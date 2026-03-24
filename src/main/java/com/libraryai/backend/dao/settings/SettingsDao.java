package com.libraryai.backend.dao.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.AIConfig;
import com.libraryai.backend.config.DatabaseConnection;
import com.libraryai.backend.dao.file.UploadedFileDao;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * SETTINGSDAO - Acceso a BD para configuraciones de usuario
 *
 * Operaciones:
 * - getInstruccionIA:     Lee InstruccionPermanenteIA de la tabla Usuario
 * - updateInstruccionIA:  Actualiza InstruccionPermanenteIA en la tabla Usuario
 * - getSuscripcionActiva: Lee Suscripcion + Plan del usuario activo
 */
public class SettingsDao {
    private static final String PLAN_FREE_CODE = "GRATUITO";
    private static final String PLAN_PREMIUM_CODE = "PREMIUM";
    private static final String PLAN_FREE_NAME = "Plan Gratuito";
    private static final String PLAN_PREMIUM_NAME = "Plan Premium";
    private static final String ROLE_FREE = "Gratuito";
    private static final String ROLE_PREMIUM = "Premium";
    private static final long PLAN_FREE_STORAGE_MB = 500L;
    private static final long PLAN_PREMIUM_STORAGE_MB = 2048L;
    private static final BigDecimal PLAN_FREE_PRICE = BigDecimal.ZERO;
    private static final BigDecimal PLAN_PREMIUM_PRICE = new BigDecimal("9.99");
    private static final String PLAN_FREE_COLOR = "#4ECDC4";
    private static final String PLAN_PREMIUM_COLOR = "#FFD700";

    /**
     * Garantiza que el catálogo mínimo de planes exista antes de operar con
     * suscripciones, vistas admin o cambios de plan.
     */
    public static void ensureDefaultPlans() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int freePlanId = findOrCreatePlan(conn, PLAN_FREE_CODE, PLAN_FREE_NAME);
            int premiumPlanId = findOrCreatePlan(conn, PLAN_PREMIUM_CODE, PLAN_PREMIUM_NAME);
            ensurePlanRoleMapping(conn, freePlanId, ROLE_FREE);
            ensurePlanRoleMapping(conn, premiumPlanId, ROLE_PREMIUM);
            conn.commit();
        } catch (Exception e) {
            System.out.println("Error al garantizar planes por defecto: " + e.getMessage());
        }
    }


    /**
     * Obtiene la instrucción permanente de IA del usuario.
     * Campo: Usuario.InstruccionPermanenteIA
     */
    public static JsonObject getInstruccionIA(int userId) {
        JsonObject response = new JsonObject();
        String sql = "SELECT InstruccionPermanenteIA FROM Usuario WHERE PK_UsuarioID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String instruccion = rs.getString("InstruccionPermanenteIA");
                response.addProperty("status", 200);
                response.addProperty("instruccion", instruccion != null ? instruccion : "");
            } else {
                response.addProperty("status", 404);
                response.addProperty("Mensaje", "Usuario no encontrado");
            }

        } catch (Exception e) {
            System.out.println("Error en getInstruccionIA: " + e.getMessage());
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "Error interno al obtener instrucción");
        }

        return response;
    }

    /**
     * Actualiza la instrucción permanente de IA del usuario.
     * Campo: Usuario.InstruccionPermanenteIA
     */
    public static JsonObject updateInstruccionIA(int userId, String instruccion) {
        JsonObject response = new JsonObject();
        String sql = "UPDATE Usuario SET InstruccionPermanenteIA = ? WHERE PK_UsuarioID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, instruccion);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                response.addProperty("status", 200);
                response.addProperty("Mensaje", "Instrucción actualizada correctamente");
            } else {
                response.addProperty("status", 404);
                response.addProperty("Mensaje", "Usuario no encontrado");
            }

        } catch (Exception e) {
            System.out.println("Error en updateInstruccionIA: " + e.getMessage());
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "Error interno al actualizar instrucción");
        }

        return response;
    }

    /**
     * Obtiene la suscripción activa del usuario junto con los datos del plan.
     * Tablas: Suscripcion JOIN Plan
     * Si no tiene suscripción activa, devuelve estado por defecto (Sin Plan).
     */
    public static JsonObject getSuscripcionActiva(int userId) {
        JsonObject response = new JsonObject();
        String sql = """
            SELECT s.FechaInicio, s.FechaFin, s.Estado, s.RenovacionAutomatica,
                   p.PK_PlanID, p.CodigoPlan, p.NombrePlan, p.AlmacenamientoMaxMB, p.Precio,
                   p.ColorHex, p.FK_ModeloPreferidoID,
                   COALESCE((
                       SELECT r.NombreRol
                       FROM PlanRol pr
                       INNER JOIN Rol r ON r.PK_RolID = pr.FK_RolID
                       WHERE pr.FK_PlanID = p.PK_PlanID
                       ORDER BY CASE
                           WHEN r.NombreRol = 'Premium' THEN 0
                           WHEN r.NombreRol = 'Gratuito' THEN 1
                           ELSE 2
                       END
                       LIMIT 1
                   ), CASE
                       WHEN UPPER(COALESCE(p.CodigoPlan, '')) = 'PREMIUM' THEN 'Premium'
                       ELSE 'Gratuito'
                   END) AS RolPlan
            FROM Suscripcion s
            JOIN Plan p ON s.FK_PlanID = p.PK_PlanID
            WHERE s.FK_UsuarioID = ? AND s.Estado = 'Activa'
            ORDER BY s.FechaInicio DESC
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String planCode = resolvePlanCode(rs.getString("CodigoPlan"), rs.getString("NombrePlan"));
                String planName = rs.getString("NombrePlan");
                String normalizedPlan = rs.getString("RolPlan");
                if (normalizedPlan == null || normalizedPlan.isBlank()) {
                    normalizedPlan = planCodeToRoleName(planCode);
                }
                Long almacenamientoMax = rs.getObject("AlmacenamientoMaxMB") == null
                        ? (PLAN_PREMIUM_CODE.equals(planCode) ? PLAN_PREMIUM_STORAGE_MB : null)
                        : ((Number) rs.getObject("AlmacenamientoMaxMB")).longValue();
                boolean almacenamientoIlimitado = almacenamientoMax == null || almacenamientoMax <= 0;
                double usedStorageMb = bytesToMb(UploadedFileDao.sumBytesByUser(userId));
                response.addProperty("status", 200);
                response.addProperty("planId", rs.getInt("PK_PlanID"));
                response.addProperty("codigoPlan", planCode);
                response.addProperty("plan", normalizedPlan);
                response.addProperty("nombrePlan", planName);
                response.addProperty(
                        "colorHex",
                        rs.getString("ColorHex") != null ? rs.getString("ColorHex") : getDefaultPlanColor(planCode));
                if (rs.getObject("FK_ModeloPreferidoID") != null) {
                    response.addProperty("modeloPreferidoId", rs.getInt("FK_ModeloPreferidoID"));
                }
                response.addProperty("limiteAlmacenamientoMb", almacenamientoIlimitado ? 0 : almacenamientoMax);
                response.addProperty("almacenamientoUsadoMb", usedStorageMb);
                response.addProperty("almacenamiento", almacenamientoIlimitado ? 0 : almacenamientoMax);
                response.addProperty("almacenamientoIlimitado", almacenamientoIlimitado);
                response.addProperty("precio", rs.getDouble("Precio"));
                response.addProperty("estado", rs.getString("Estado"));
                response.addProperty("fechaInicio", rs.getString("FechaInicio"));
                String fechaFin = rs.getString("FechaFin");
                response.addProperty("fechaFin", fechaFin != null ? fechaFin : "Sin vencimiento");
                response.addProperty("renovacionAutomatica", rs.getBoolean("RenovacionAutomatica"));
            } else {
                // Sin suscripción activa: valores por defecto
                double usedStorageMb = bytesToMb(UploadedFileDao.sumBytesByUser(userId));
                response.addProperty("status", 200);
                response.addProperty("planId", 0);
                response.addProperty("codigoPlan", PLAN_FREE_CODE);
                response.addProperty("plan", "Gratuito");
                response.addProperty("nombrePlan", PLAN_FREE_NAME);
                response.addProperty("colorHex", PLAN_FREE_COLOR);
                response.addProperty("limiteAlmacenamientoMb", 500);
                response.addProperty("almacenamientoUsadoMb", usedStorageMb);
                response.addProperty("almacenamiento", 500);
                response.addProperty("almacenamientoIlimitado", false);
                response.addProperty("precio", 0.0);
                response.addProperty("estado", "Inactiva");
                response.addProperty("fechaInicio", "N/A");
                response.addProperty("fechaFin", "N/A");
                response.addProperty("renovacionAutomatica", false);
            }

        } catch (Exception e) {
            System.out.println("Error en getSuscripcionActiva: " + e.getMessage());
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "Error interno al obtener suscripción");
        }

        return response;
    }

    /**
     * Devuelve el catálogo de planes visible para el usuario en settings.
     * Incluye planes activos y, como respaldo, el plan actual si quedó inactivo.
     */
    public static JsonObject getPlanCatalog(int currentPlanId, String currentPlanCode) {
        JsonObject response = new JsonObject();
        String normalizedCurrentCode = currentPlanCode == null ? "" : currentPlanCode.trim().toUpperCase(Locale.ROOT);
        String sql = """
            SELECT p.PK_PlanID, p.CodigoPlan, p.NombrePlan, p.AlmacenamientoMaxMB, p.Precio,
                   p.ColorHex, p.Activo, p.FK_ModeloPreferidoID,
                   COALESCE((
                       SELECT r.NombreRol
                       FROM PlanRol pr
                       INNER JOIN Rol r ON r.PK_RolID = pr.FK_RolID
                       WHERE pr.FK_PlanID = p.PK_PlanID
                       ORDER BY CASE
                           WHEN r.NombreRol = 'Premium' THEN 0
                           WHEN r.NombreRol = 'Gratuito' THEN 1
                           ELSE 2
                       END
                       LIMIT 1
                   ), CASE
                       WHEN UPPER(COALESCE(p.CodigoPlan, '')) = 'PREMIUM' THEN 'Premium'
                       ELSE 'Gratuito'
                   END) AS RolBase,
                   m.NombreModelo AS ModeloPreferidoNombre,
                   m.Version AS ModeloPreferidoVersion
            FROM Plan p
            LEFT JOIN ModeloIA m
                ON m.PK_ModeloID = p.FK_ModeloPreferidoID
               AND m.Estado = 'Activo'
            WHERE p.Activo = TRUE OR p.PK_PlanID = ?
            ORDER BY CASE
                       WHEN p.PK_PlanID = ? THEN 0
                       ELSE 1
                     END,
                     CASE
                       WHEN COALESCE(p.Precio, 0) = 0 THEN 0
                       ELSE 1
                     END,
                     COALESCE(p.Precio, 0) ASC,
                     p.PK_PlanID ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentPlanId);
            stmt.setInt(2, currentPlanId);
            ResultSet rs = stmt.executeQuery();

            JsonArray planes = new JsonArray();
            while (rs.next()) {
                JsonObject plan = new JsonObject();
                String code = resolvePlanCode(rs.getString("CodigoPlan"), rs.getString("NombrePlan"));
                long storageMb = rs.getObject("AlmacenamientoMaxMB") == null
                        ? 0L
                        : ((Number) rs.getObject("AlmacenamientoMaxMB")).longValue();
                String roleBase = rs.getString("RolBase");
                boolean current = currentPlanId > 0
                        ? rs.getInt("PK_PlanID") == currentPlanId
                        : code.equalsIgnoreCase(normalizedCurrentCode);

                plan.addProperty("id", rs.getInt("PK_PlanID"));
                plan.addProperty("codigoPlan", code);
                plan.addProperty("nombrePlan", rs.getString("NombrePlan"));
                plan.addProperty("rolBase", roleBase == null || roleBase.isBlank() ? planCodeToRoleName(code) : roleBase);
                plan.addProperty("almacenamientoMaxMb", storageMb);
                plan.addProperty("almacenamientoIlimitado", storageMb <= 0);
                plan.addProperty("precio", rs.getBigDecimal("Precio"));
                plan.addProperty("activo", rs.getBoolean("Activo"));
                plan.addProperty("esActual", current);
                plan.addProperty("colorHex", rs.getString("ColorHex") != null
                        ? rs.getString("ColorHex")
                        : getDefaultPlanColor(code));
                if (rs.getObject("FK_ModeloPreferidoID") != null) {
                    plan.addProperty("modeloPreferidoId", rs.getInt("FK_ModeloPreferidoID"));
                }
                plan.addProperty(
                        "modeloPreferidoNombre",
                        rs.getString("ModeloPreferidoNombre") == null ? "" : rs.getString("ModeloPreferidoNombre"));
                plan.addProperty(
                        "modeloPreferidoVersion",
                        rs.getString("ModeloPreferidoVersion") == null ? "" : rs.getString("ModeloPreferidoVersion"));
                planes.add(plan);
            }

            response.addProperty("status", 200);
            response.add("planes", planes);
            response.addProperty("total", planes.size());
        } catch (Exception e) {
            System.out.println("Error en getPlanCatalog: " + e.getMessage());
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "Error interno al obtener los planes disponibles");
        }

        return response;
    }

    /**
     * Simula un cambio de plan creando suscripción, pago simulado y rol efectivo.
     */
    public static JsonObject simulateSubscriptionChange(int userId, Integer requestedPlanId, String requestedPlan) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                PlanSelection requestedSelection = resolvePlanSelection(conn, requestedPlanId, requestedPlan);
                if (requestedSelection == null) {
                    conn.rollback();
                    response.addProperty("status", 404);
                    response.addProperty("Mensaje", "El plan solicitado no está disponible");
                    return response;
                }

                String roleName = resolvePlanRole(conn, requestedSelection.id, requestedSelection.code);
                cancelActiveSubscriptions(conn, userId);
                int subscriptionId = createSubscription(conn, userId, requestedSelection.id);

                if (requestedSelection.price.compareTo(BigDecimal.ZERO) > 0) {
                    createSimulatedPayment(conn, subscriptionId, requestedSelection.price);
                }

                replaceUserRole(conn, userId, roleName);
                conn.commit();
            } catch (JsonEarlyReturn earlyReturn) {
                conn.rollback();
                return earlyReturn.response;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            JsonObject subscription = getSuscripcionActiva(userId);
            if (!subscription.has("status") || subscription.get("status").getAsInt() != 200) {
                return subscription;
            }

            subscription.addProperty("Mensaje",
                    subscription.has("nombrePlan")
                            ? "Plan " + subscription.get("nombrePlan").getAsString() + " activado correctamente"
                            : "Plan activado correctamente");
            subscription.addProperty("rol", subscription.has("plan") ? subscription.get("plan").getAsString() : ROLE_FREE);
            subscription.addProperty("simulado", true);
            return subscription;
        } catch (Exception e) {
            System.out.println("Error en simulateSubscriptionChange: " + e.getMessage());
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "Error interno al simular el cambio de plan");
            return response;
        }
    }

    /**
     * Asigna la suscripción gratuita inicial a un usuario recién creado.
     * Si el usuario ya tiene una suscripción activa, la conserva.
     */
    public static JsonObject assignDefaultFreeSubscription(int userId) {
        JsonObject response = new JsonObject();

        if (userId <= 0) {
            response.addProperty("status", 400);
            response.addProperty("Mensaje", "Usuario inválido para asignar suscripción inicial");
            return response;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Integer activeSubscriptionId = findActiveSubscriptionId(conn, userId);
                if (activeSubscriptionId == null) {
                    int freePlanId = findOrCreatePlan(conn, PLAN_FREE_CODE, PLAN_FREE_NAME);
                    createSubscription(conn, userId, freePlanId);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            JsonObject subscription = getSuscripcionActiva(userId);
            if (!subscription.has("status") || subscription.get("status").getAsInt() != 200) {
                return subscription;
            }

            subscription.addProperty("Mensaje", "Suscripción gratuita inicial asignada correctamente");
            return subscription;
        } catch (Exception e) {
            System.out.println("Error en assignDefaultFreeSubscription: " + e.getMessage());
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "No fue posible asignar la suscripción gratuita inicial");
            return response;
        }
    }

    private static double bytesToMb(long bytes) {
        return Math.round((bytes / 1024d / 1024d) * 100.0d) / 100.0d;
    }

    /**
     * Obtiene la versión actual del modelo IA (RF_23).
     */
    public static JsonObject getVersionActual() {
        return getVersionActual(ROLE_FREE);
    }

    /**
     * Resuelve el modelo preferente según el plan solicitado.
     */
    public static JsonObject getVersionActual(String plan) {
        return getVersionActual(null, plan);
    }

    /**
     * Resuelve el modelo preferente según el plan solicitado y el modelo principal
     * configurado en el plan, si existe.
     */
    public static JsonObject getVersionActual(Integer planId, String plan) {
        JsonObject configuredModel = getPlanPreferredModel(planId);
        if (configuredModel != null) {
            configuredModel.addProperty("status", 200);
            return configuredModel;
        }

        return getVersionActualByRoleFallback(plan);
    }

    /**
     * Obtiene modelos disponibles según el plan del usuario (RF_23).
     */
    public static JsonObject getModelosPorPlan(String plan) {
        return getModelosPorPlan(null, plan);
    }

    /**
     * Obtiene modelos disponibles según plan y prioriza el modelo principal del plan
     * cuando existe.
     */
    public static JsonObject getModelosPorPlan(Integer planId, String plan) {
        JsonObject response = new JsonObject();
        Integer preferredModelId = findPreferredModelIdByPlan(planId);

        String sql;
        if (isPremiumPlan(plan)) {
            sql = """
                SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, EsGratuito
                FROM ModeloIA
                WHERE Estado = 'Activo'
                ORDER BY CASE
                    WHEN PK_ModeloID = ? THEN 0
                    WHEN LOWER(NombreModelo) = LOWER(?) THEN 1
                    WHEN EsGratuito = FALSE THEN 2
                    ELSE 3
                END, PK_ModeloID ASC
            """;
        } else {
            sql = """
                SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, EsGratuito
                FROM ModeloIA
                WHERE Estado = 'Activo' AND EsGratuito = TRUE
                ORDER BY CASE
                    WHEN PK_ModeloID = ? THEN 0
                    WHEN LOWER(NombreModelo) = LOWER(?) THEN 1
                    ELSE 2
                END, PK_ModeloID ASC
            """;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, preferredModelId == null ? -1 : preferredModelId);
            stmt.setString(2, getPreferredModelNameForPlan(plan));
            ResultSet rs = stmt.executeQuery();

            JsonArray modelos = new JsonArray();

            while (rs.next()) {
                modelos.add(buildModelJson(rs));
            }

            response.addProperty("status", 200);
            response.add("modelos", modelos);
            response.addProperty("total", modelos.size());

        } catch (Exception e) {
            System.out.println("Error en getModelosPorPlan: " + e.getMessage());
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "Error interno al obtener modelos");
        }

        return response;
    }

    /**
     * Busca un modelo IA específico si se encuentra activo en catálogo.
     */
    public static JsonObject getModeloById(int modelId) {
        JsonObject response = new JsonObject();
        String sql = """
            SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, EsGratuito, Estado
            FROM ModeloIA
            WHERE PK_ModeloID = ? AND Estado = 'Activo'
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, modelId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                response.addProperty("status", 200);
                response.add("modelo", buildModelJson(rs));
            } else {
                response.addProperty("status", 404);
                response.addProperty("Mensaje", "Modelo no encontrado o inactivo");
            }
        } catch (Exception e) {
            System.out.println("Error en getModeloById: " + e.getMessage());
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "Error interno al obtener modelo");
        }

        return response;
    }

    private static int findOrCreatePlan(Connection conn, String planCode, String planName) throws SQLException {
        Integer existingId = findPlanByCode(conn, planCode);
        if (existingId != null) {
            return existingId;
        }

        return createDefaultPlan(conn, planCode, planName);
    }

    private static Integer findPlanByCode(Connection conn, String planCode) throws SQLException {
        String sql = "SELECT PK_PlanID FROM Plan WHERE CodigoPlan = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, planCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("PK_PlanID");
            }
        }

        return null;
    }

    private static int createDefaultPlan(Connection conn, String planCode, String planName) throws SQLException {
        String sql = """
            INSERT INTO Plan(CodigoPlan, NombrePlan, AlmacenamientoMaxMB, Precio, ColorHex, Activo)
            VALUES (?, ?, ?, ?, ?, TRUE)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, planCode);
            stmt.setString(2, planName);

            if (PLAN_PREMIUM_CODE.equals(planCode)) {
                stmt.setLong(3, PLAN_PREMIUM_STORAGE_MB);
                stmt.setBigDecimal(4, PLAN_PREMIUM_PRICE);
            } else {
                stmt.setLong(3, PLAN_FREE_STORAGE_MB);
                stmt.setBigDecimal(4, PLAN_FREE_PRICE);
            }
            stmt.setString(5, getDefaultPlanColor(planCode));

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();

            if (keys.next()) {
                return keys.getInt(1);
            }
        }

        throw new SQLException("No fue posible crear el plan " + planName);
    }

    private static void ensurePlanRoleMapping(Connection conn, int planId, String roleName) throws SQLException {
        if (hasPlanRoleMapping(conn, planId, roleName)) {
            return;
        }

        Integer roleId = findRoleIdByName(conn, roleName);
        if (roleId == null) {
            throw new SQLException("No fue posible resolver el rol base " + roleName);
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO PlanRol(FK_PlanID, FK_RolID) VALUES (?, ?)")) {
            stmt.setInt(1, planId);
            stmt.setInt(2, roleId);
            stmt.executeUpdate();
        }
    }

    private static boolean hasPlanRoleMapping(Connection conn, int planId, String roleName) throws SQLException {
        String sql = """
            SELECT 1
            FROM PlanRol pr
            INNER JOIN Rol r ON r.PK_RolID = pr.FK_RolID
            WHERE pr.FK_PlanID = ? AND r.NombreRol = ?
            LIMIT 1
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            stmt.setString(2, roleName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private static Integer findRoleIdByName(Connection conn, String roleName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT PK_RolID FROM Rol WHERE NombreRol = ? LIMIT 1")) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("PK_RolID");
            }
        }

        return null;
    }

    private static void cancelActiveSubscriptions(Connection conn, int userId) throws SQLException {
        String sql = """
            UPDATE Suscripcion
            SET Estado = 'Cancelada',
                FechaFin = COALESCE(FechaFin, CURRENT_TIMESTAMP)
            WHERE FK_UsuarioID = ? AND Estado = 'Activa'
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private static Integer findActiveSubscriptionId(Connection conn, int userId) throws SQLException {
        String sql = """
            SELECT PK_SuscripcionID
            FROM Suscripcion
            WHERE FK_UsuarioID = ? AND Estado = 'Activa'
            ORDER BY FechaInicio DESC
            LIMIT 1
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("PK_SuscripcionID");
            }
        }

        return null;
    }

    private static int createSubscription(Connection conn, int userId, int planId) throws SQLException {
        String sql = """
            INSERT INTO Suscripcion(FK_UsuarioID, FK_PlanID, FechaInicio, FechaFin, Estado, RenovacionAutomatica)
            VALUES (?, ?, CURRENT_TIMESTAMP, ?, 'Activa', FALSE)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, planId);
            stmt.setNull(3, Types.TIMESTAMP);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        }

        throw new SQLException("No fue posible crear la suscripción");
    }

    private static void createSimulatedPayment(Connection conn, int subscriptionId, BigDecimal amount) throws SQLException {
        String sql = """
            INSERT INTO Pago(FK_SuscripcionID, Pasarela, EstadoPago, ReferenciaExterna, Monto, FechaPago)
            VALUES (?, 'Otra', 'Completado', ?, ?, CURRENT_TIMESTAMP)
            """;

        String reference = "SIM-" + subscriptionId + "-" + System.currentTimeMillis();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, subscriptionId);
            stmt.setString(2, reference);
            stmt.setBigDecimal(3, amount);
            stmt.executeUpdate();
        }
    }

    private static void replaceUserRole(Connection conn, int userId, String roleName) throws SQLException {
        String deleteSql = "DELETE FROM UsuarioRol WHERE FK_UsuarioID = ?";
        String insertSql = """
            INSERT INTO UsuarioRol(FK_UsuarioID, FK_RolID)
            SELECT ?, PK_RolID
            FROM Rol
            WHERE NombreRol = ?
            LIMIT 1
        """;

        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            deleteStmt.setInt(1, userId);
            deleteStmt.executeUpdate();

            insertStmt.setInt(1, userId);
            insertStmt.setString(2, roleName);
            int inserted = insertStmt.executeUpdate();

            if (inserted == 0) {
                throw new SQLException("No fue posible asignar el rol " + roleName);
            }
        }
    }

    private static PlanSelection resolvePlanSelection(Connection conn, Integer requestedPlanId, String requestedPlan)
            throws SQLException {
        if (requestedPlanId != null && requestedPlanId > 0) {
            return findSelectablePlanById(conn, requestedPlanId);
        }

        String normalizedPlan = requestedPlan == null ? "" : requestedPlan.trim();
        if (normalizedPlan.isBlank()) {
            return null;
        }

        PlanSelection selection = findSelectablePlanByCodeOrName(conn, normalizedPlan);
        if (selection != null) {
            return selection;
        }

        String normalizedLegacyRole = normalizePlanName(normalizedPlan);
        if (normalizedLegacyRole == null) {
            return null;
        }

        String fallbackCode = ROLE_PREMIUM.equalsIgnoreCase(normalizedLegacyRole) ? PLAN_PREMIUM_CODE : PLAN_FREE_CODE;
        return findSelectablePlanByCodeOrName(conn, fallbackCode);
    }

    private static PlanSelection findSelectablePlanById(Connection conn, int planId) throws SQLException {
        String sql = """
            SELECT PK_PlanID, CodigoPlan, NombrePlan, Precio, Activo
            FROM Plan
            WHERE PK_PlanID = ?
            LIMIT 1
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }

            if (!rs.getBoolean("Activo")) {
                throw buildPlanSelectionError(409, "El plan seleccionado está inactivo");
            }

            return new PlanSelection(
                    rs.getInt("PK_PlanID"),
                    resolvePlanCode(rs.getString("CodigoPlan"), rs.getString("NombrePlan")),
                    rs.getString("NombrePlan"),
                    rs.getBigDecimal("Precio"));
        }
    }

    private static PlanSelection findSelectablePlanByCodeOrName(Connection conn, String planValue) throws SQLException {
        String sql = """
            SELECT PK_PlanID, CodigoPlan, NombrePlan, Precio, Activo
            FROM Plan
            WHERE UPPER(COALESCE(CodigoPlan, '')) = UPPER(?)
               OR LOWER(COALESCE(NombrePlan, '')) = LOWER(?)
            ORDER BY Activo DESC, PK_PlanID ASC
            LIMIT 1
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, planValue);
            stmt.setString(2, planValue);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }

            if (!rs.getBoolean("Activo")) {
                throw buildPlanSelectionError(409, "El plan seleccionado está inactivo");
            }

            return new PlanSelection(
                    rs.getInt("PK_PlanID"),
                    resolvePlanCode(rs.getString("CodigoPlan"), rs.getString("NombrePlan")),
                    rs.getString("NombrePlan"),
                    rs.getBigDecimal("Precio"));
        }
    }

    private static JsonEarlyReturn buildPlanSelectionError(int status, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", status);
        response.addProperty("Mensaje", message);
        return new JsonEarlyReturn(response);
    }

    private static String normalizePlanName(String plan) {
        String planCode = normalizePlanCode(plan);
        if (PLAN_PREMIUM_CODE.equals(planCode)) {
            return ROLE_PREMIUM;
        }

        if (PLAN_FREE_CODE.equals(planCode)) {
            return ROLE_FREE;
        }

        return null;
    }

    private static String normalizePlanCode(String plan) {
        if (plan == null) {
            return null;
        }

        String normalized = plan.trim().toLowerCase(Locale.ROOT);

        if (normalized.isBlank()) {
            return null;
        }

        if (normalized.contains("premium")) {
            return PLAN_PREMIUM_CODE;
        }

        if (normalized.contains("gratuito") || normalized.contains("basico") || normalized.contains("básico")
                || normalized.contains("free") || normalized.contains("basic")) {
            return PLAN_FREE_CODE;
        }

        return null;
    }

    private static boolean isPremiumPlan(String plan) {
        return PLAN_PREMIUM_CODE.equals(normalizePlanCode(plan));
    }

    private static String resolvePlanCode(String storedCode, String planName) {
        if (storedCode != null && !storedCode.isBlank()) {
            return storedCode.trim().toUpperCase(Locale.ROOT);
        }

        String normalizedCode = normalizePlanCode(planName);
        return normalizedCode == null ? PLAN_FREE_CODE : normalizedCode;
    }

    private static String planCodeToRoleName(String planCode) {
        return PLAN_PREMIUM_CODE.equals(resolvePlanCode(planCode, null)) ? ROLE_PREMIUM : ROLE_FREE;
    }

    private static String resolvePlanRole(Connection conn, int planId, String planCode) throws SQLException {
        String sql = """
            SELECT r.NombreRol
            FROM PlanRol pr
            INNER JOIN Rol r ON r.PK_RolID = pr.FK_RolID
            WHERE pr.FK_PlanID = ?
            ORDER BY CASE
                WHEN r.NombreRol = 'Premium' THEN 0
                WHEN r.NombreRol = 'Gratuito' THEN 1
                ELSE 2
            END
            LIMIT 1
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("NombreRol");
            }
        }

        return planCodeToRoleName(planCode);
    }

    private static JsonObject buildModelJson(ResultSet rs) throws java.sql.SQLException {
        JsonObject modelo = new JsonObject();
        String nombre = rs.getString("NombreModelo");
        String version = rs.getString("Version");
        String descripcion = rs.getString("Descripcion") != null ? rs.getString("Descripcion") : "";
        String notasVersion = rs.getString("NotasVersion") != null ? rs.getString("NotasVersion") : "";
        modelo.addProperty("id", rs.getInt("PK_ModeloID"));
        modelo.addProperty("nombre", nombre);
        modelo.addProperty("version", version);
        modelo.addProperty("descripcion", descripcion);
        modelo.addProperty("notasVersion", notasVersion);
        modelo.addProperty("changelog", notasVersion);
        modelo.addProperty("activo", nombre);
        modelo.addProperty("gratuito", rs.getBoolean("EsGratuito"));
        return modelo;
    }

    private static JsonObject getPlanPreferredModel(Integer planId) {
        if (planId == null || planId <= 0) {
            return null;
        }

        String sql = """
            SELECT m.PK_ModeloID, m.NombreModelo, m.Version, m.Descripcion, m.NotasVersion, m.EsGratuito
            FROM Plan p
            INNER JOIN ModeloIA m ON m.PK_ModeloID = p.FK_ModeloPreferidoID
            WHERE p.PK_PlanID = ? AND m.Estado = 'Activo'
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return buildModelJson(rs);
            }
        } catch (Exception e) {
            System.out.println("Error en getPlanPreferredModel: " + e.getMessage());
        }

        return null;
    }

    private static Integer findPreferredModelIdByPlan(Integer planId) {
        if (planId == null || planId <= 0) {
            return null;
        }

        String sql = "SELECT FK_ModeloPreferidoID FROM Plan WHERE PK_PlanID = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getObject("FK_ModeloPreferidoID") != null) {
                return rs.getInt("FK_ModeloPreferidoID");
            }
        } catch (Exception e) {
            System.out.println("Error en findPreferredModelIdByPlan: " + e.getMessage());
        }

        return null;
    }

    private static JsonObject getVersionActualByRoleFallback(String plan) {
        JsonObject response = new JsonObject();
        String sql = isPremiumPlan(plan)
                ? """
            SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, Estado, EsGratuito
            FROM ModeloIA
            WHERE Estado = 'Activo'
            ORDER BY CASE
                WHEN LOWER(NombreModelo) = LOWER(?) THEN 0
                WHEN EsGratuito = FALSE THEN 1
                ELSE 2
            END, PK_ModeloID ASC
            LIMIT 1
        """
                : """
            SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, Estado, EsGratuito
            FROM ModeloIA
            WHERE Estado = 'Activo' AND EsGratuito = TRUE
            ORDER BY CASE
                WHEN LOWER(NombreModelo) = LOWER(?) THEN 0
                ELSE 1
            END, PK_ModeloID ASC
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, getPreferredModelNameForPlan(plan));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                response = buildModelJson(rs);
                response.addProperty("status", 200);
            } else {
                return buildConfiguredModelFallback(plan);
            }

        } catch (Exception e) {
            System.out.println("Error en getVersionActualByRoleFallback: " + e.getMessage());
            return buildConfiguredModelFallback(plan);
        }

        return response;
    }

    private static String getDefaultPlanColor(String planCode) {
        return PLAN_PREMIUM_CODE.equals(resolvePlanCode(planCode, null)) ? PLAN_PREMIUM_COLOR : PLAN_FREE_COLOR;
    }

    private static String getPreferredModelNameForPlan(String plan) {
        if (isPremiumPlan(plan) && AIConfig.PREMIUM_MODEL != null && !AIConfig.PREMIUM_MODEL.isBlank()) {
            return AIConfig.PREMIUM_MODEL.trim();
        }

        if (AIConfig.FREE_MODEL != null && !AIConfig.FREE_MODEL.isBlank()) {
            return AIConfig.FREE_MODEL.trim();
        }

        return AIConfig.MODEL_AI == null ? "" : AIConfig.MODEL_AI.trim();
    }

    private static JsonObject buildConfiguredModelFallback(String plan) {
        JsonObject response = new JsonObject();
        String configuredModel = getPreferredModelNameForPlan(plan);

        if (configuredModel.isBlank()) {
            response.addProperty("status", 404);
            response.addProperty("Mensaje", "No hay modelo activo");
            return response;
        }

        response.addProperty("status", 200);
        response.addProperty("nombre", configuredModel);
        response.addProperty("version", "Configuración global");
        response.addProperty("descripcion", "Modelo operativo configurado desde el entorno del backend.");
        response.addProperty("changelog", "Sin registro cargado en la tabla ModeloIA.");
        response.addProperty("notasVersion", "Sin registro cargado en la tabla ModeloIA.");
        response.addProperty("activo", configuredModel);
        response.addProperty("gratuito", !isPremiumPlan(plan));
        return response;
    }

    private static final class PlanSelection {
        private final int id;
        private final String code;
        private final String name;
        private final BigDecimal price;

        private PlanSelection(int id, String code, String name, BigDecimal price) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.price = price;
        }
    }

    private static final class JsonEarlyReturn extends RuntimeException {
        private final JsonObject response;

        private JsonEarlyReturn(JsonObject response) {
            this.response = response;
        }
    }
}
