package com.libraryai.backend.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.AIConfig;
import com.libraryai.backend.config.DatabaseConnection;
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
 * - getSuscripcionActiva: Lee Suscripcion + PlanSuscripcion del usuario activo
 */
public class SettingsDao {
    private static final String PLAN_FREE_NAME = "Plan Gratuito";
    private static final String PLAN_PREMIUM_NAME = "Plan Premium";
    private static final String ROLE_FREE = "Gratuito";
    private static final String ROLE_PREMIUM = "Premium";
    private static final long PLAN_FREE_STORAGE_MB = 500L;
    private static final long PLAN_PREMIUM_STORAGE_MB = 2048L;
    private static final BigDecimal PLAN_FREE_PRICE = BigDecimal.ZERO;
    private static final BigDecimal PLAN_PREMIUM_PRICE = new BigDecimal("9.99");

    /**
     * Garantiza que el catálogo mínimo de planes exista antes de operar con
     * suscripciones, vistas admin o cambios de plan.
     */
    public static void ensureDefaultPlans() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            findOrCreatePlan(conn, PLAN_FREE_NAME);
            findOrCreatePlan(conn, PLAN_PREMIUM_NAME);
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
     * Tablas: Suscripcion JOIN PlanSuscripcion
     * Si no tiene suscripción activa, devuelve estado por defecto (Sin Plan).
     */
    public static JsonObject getSuscripcionActiva(int userId) {
        JsonObject response = new JsonObject();
        String sql = """
            SELECT s.FechaInicio, s.FechaFin, s.Estado, s.RenovacionAutomatica,
                   p.NombrePlan, p.AlmacenamientoMaxMB, p.Precio
            FROM Suscripcion s
            JOIN PlanSuscripcion p ON s.FK_PlanID = p.PK_PlanID
            WHERE s.FK_UsuarioID = ? AND s.Estado = 'Activa'
            ORDER BY s.FechaInicio DESC
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String planName = rs.getString("NombrePlan");
                String normalizedPlan = normalizePlanName(planName);
                Long almacenamientoMax = rs.getObject("AlmacenamientoMaxMB") == null
                        ? (isPremiumPlan(planName) ? PLAN_PREMIUM_STORAGE_MB : null)
                        : ((Number) rs.getObject("AlmacenamientoMaxMB")).longValue();
                boolean almacenamientoIlimitado = almacenamientoMax == null || almacenamientoMax <= 0;
                double usedStorageMb = bytesToMb(UploadedFileDao.sumBytesByUser(userId));
                response.addProperty("status", 200);
                response.addProperty("plan", normalizedPlan);
                response.addProperty("nombrePlan", planName);
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
                response.addProperty("plan", "Gratuito");
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
     * Simula un cambio de plan creando suscripción, pago simulado y rol efectivo.
     */
    public static JsonObject simulateSubscriptionChange(int userId, String requestedPlan) {
        JsonObject response = new JsonObject();
        String normalizedPlan = normalizePlanName(requestedPlan);

        if (normalizedPlan == null) {
            response.addProperty("status", 400);
            response.addProperty("Mensaje", "El plan solicitado no es válido");
            return response;
        }

        boolean premiumRequested = isPremiumPlan(normalizedPlan);
        String planName = premiumRequested ? PLAN_PREMIUM_NAME : PLAN_FREE_NAME;
        String roleName = premiumRequested ? ROLE_PREMIUM : ROLE_FREE;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int planId = findOrCreatePlan(conn, planName);
                cancelActiveSubscriptions(conn, userId);
                int subscriptionId = createSubscription(conn, userId, planId);

                if (premiumRequested) {
                    createSimulatedPayment(conn, subscriptionId);
                }

                replaceUserRole(conn, userId, roleName);
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

            subscription.addProperty("Mensaje",
                    premiumRequested
                            ? "Plan Premium activado correctamente"
                            : "Plan Gratuito activado correctamente");
            subscription.addProperty("rol", roleName);
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
                    int freePlanId = findOrCreatePlan(conn, PLAN_FREE_NAME);
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
            System.out.println("Error en getVersionActual: " + e.getMessage());
            return buildConfiguredModelFallback(plan);
        }

        return response;
    }

    /**
     * Obtiene modelos disponibles según el plan del usuario (RF_23).
     */
    public static JsonObject getModelosPorPlan(String plan) {
        JsonObject response = new JsonObject();
        
        // Según el RF, los modelos disponibles dependen del plan
        String sql;
        if (isPremiumPlan(plan)) {
            // Premium tiene acceso a todos los modelos
            sql = """
                SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, EsGratuito
                FROM ModeloIA
                WHERE Estado = 'Activo'
                ORDER BY CASE
                    WHEN LOWER(NombreModelo) = LOWER(?) THEN 0
                    WHEN EsGratuito = FALSE THEN 1
                    ELSE 2
                END, PK_ModeloID ASC
            """;
        } else {
            // Gratuito solo tiene acceso a modelos gratuitos
            sql = """
                SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, EsGratuito
                FROM ModeloIA
                WHERE Estado = 'Activo' AND EsGratuito = TRUE
                ORDER BY CASE
                    WHEN LOWER(NombreModelo) = LOWER(?) THEN 0
                    ELSE 1
                END, PK_ModeloID ASC
            """;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, getPreferredModelNameForPlan(plan));
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

    private static int findOrCreatePlan(Connection conn, String planName) throws SQLException {
        Integer existingId = findPlanByName(conn, planName);
        if (existingId != null) {
            return existingId;
        }

        return createDefaultPlan(conn, planName);
    }

    private static Integer findPlanByName(Connection conn, String planName) throws SQLException {
        String sql = "SELECT PK_PlanID FROM PlanSuscripcion WHERE NombrePlan = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, planName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("PK_PlanID");
            }
        }

        return null;
    }

    private static int createDefaultPlan(Connection conn, String planName) throws SQLException {
        String sql = """
            INSERT INTO PlanSuscripcion(NombrePlan, AlmacenamientoMaxMB, Precio, Activo)
            VALUES (?, ?, ?, TRUE)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, planName);

            if (isPremiumPlan(planName)) {
                stmt.setLong(2, PLAN_PREMIUM_STORAGE_MB);
                stmt.setBigDecimal(3, PLAN_PREMIUM_PRICE);
            } else {
                stmt.setLong(2, PLAN_FREE_STORAGE_MB);
                stmt.setBigDecimal(3, PLAN_FREE_PRICE);
            }

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();

            if (keys.next()) {
                return keys.getInt(1);
            }
        }

        throw new SQLException("No fue posible crear el plan " + planName);
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

    private static void createSimulatedPayment(Connection conn, int subscriptionId) throws SQLException {
        String sql = """
            INSERT INTO Pago(FK_SuscripcionID, Pasarela, EstadoPago, ReferenciaExterna, Monto, FechaPago)
            VALUES (?, 'Simulada', 'Completado', ?, ?, CURRENT_TIMESTAMP)
        """;

        String reference = "SIM-" + subscriptionId + "-" + System.currentTimeMillis();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, subscriptionId);
            stmt.setString(2, reference);
            stmt.setBigDecimal(3, PLAN_PREMIUM_PRICE);
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

    private static String normalizePlanName(String plan) {
        if (plan == null) {
            return null;
        }

        String normalized = plan.trim().toLowerCase(Locale.ROOT);

        if (normalized.isBlank()) {
            return null;
        }

        if (normalized.contains("premium")) {
            return ROLE_PREMIUM;
        }

        if (normalized.contains("gratuito") || normalized.contains("basico") || normalized.contains("básico")
                || normalized.contains("free") || normalized.contains("basic")) {
            return ROLE_FREE;
        }

        return null;
    }

    private static boolean isPremiumPlan(String plan) {
        return normalizePlanName(plan) != null && ROLE_PREMIUM.equals(normalizePlanName(plan));
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
}
