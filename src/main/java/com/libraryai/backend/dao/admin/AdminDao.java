package com.libraryai.backend.dao.admin;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;
import com.libraryai.backend.dao.settings.SettingsDao;

/**
 * DAO para consultas del panel de administración.
 */
public class AdminDao {

    private static final String PLAN_FREE_CODE = "GRATUITO";
    private static final String PLAN_PREMIUM_CODE = "PREMIUM";
    private static final String ROLE_FREE = "Gratuito";
    private static final String ROLE_PREMIUM = "Premium";

    private static final String SQL_SELECT_ADMIN_USERS = """
            SELECT
                u.PK_UsuarioID,
                u.Nombre,
                u.Correo,
                u.FotoPerfil,
                u.Activo,
                u.FechaRegistro,
                r.NombreRol,
                p.PK_PlanID,
                p.CodigoPlan,
                p.NombrePlan,
                s.Estado AS EstadoSuscripcion
            FROM Usuario u
            INNER JOIN UsuarioRol ur ON ur.FK_UsuarioID = u.PK_UsuarioID
            INNER JOIN Rol r ON r.PK_RolID = ur.FK_RolID
            LEFT JOIN (
                SELECT s1.*
                FROM Suscripcion s1
                INNER JOIN (
                    SELECT FK_UsuarioID, MAX(FechaInicio) AS UltimaFechaInicio
                    FROM Suscripcion
                    GROUP BY FK_UsuarioID
                ) ultima
                    ON ultima.FK_UsuarioID = s1.FK_UsuarioID
                   AND ultima.UltimaFechaInicio = s1.FechaInicio
            ) s ON s.FK_UsuarioID = u.PK_UsuarioID
            LEFT JOIN PlanSuscripcion p ON p.PK_PlanID = s.FK_PlanID
            ORDER BY u.FechaRegistro DESC, u.PK_UsuarioID DESC;
            """;

    private static final String SQL_UPDATE_USER_STATUS = """
            UPDATE Usuario
            SET Activo = ?
            WHERE PK_UsuarioID = ?;
            """;

    private static final String SQL_SELECT_USER_ROLE = """
            SELECT r.PK_RolID, r.NombreRol
            FROM UsuarioRol ur
            INNER JOIN Rol r ON r.PK_RolID = ur.FK_RolID
            WHERE ur.FK_UsuarioID = ?
            LIMIT 1;
            """;

    private static final String SQL_SELECT_ROLE_ID = """
            SELECT PK_RolID
            FROM Rol
            WHERE NombreRol = ?
            LIMIT 1;
            """;

    private static final String SQL_DELETE_USER_ROLES = """
            DELETE FROM UsuarioRol
            WHERE FK_UsuarioID = ?;
            """;

    private static final String SQL_ASSIGN_ROLE = """
            INSERT INTO UsuarioRol(FK_UsuarioID, FK_RolID)
            SELECT ?, PK_RolID
            FROM Rol
            WHERE NombreRol = ?;
            """;

    private static final String SQL_INSERT_ROLE_AUDIT = """
            INSERT INTO AuditoriaRolUsuario(
                FK_UsuarioAfectadoID,
                FK_AdminID,
                FK_RolAnteriorID,
                FK_RolNuevoID
            )
            VALUES(?, ?, ?, ?);
            """;

    private static final String SQL_SYSTEM_STATS = """
            SELECT
                (SELECT COUNT(*) FROM Usuario) AS TotalUsuarios,
                (SELECT COUNT(*) FROM Usuario WHERE Activo = TRUE) AS UsuariosActivos,
                (SELECT COUNT(*) FROM Usuario WHERE Activo = FALSE) AS UsuariosSuspendidos,
                (SELECT COUNT(*) FROM Suscripcion WHERE Estado = 'Activa'
                    AND FK_PlanID = (
                        SELECT PK_PlanID
                        FROM PlanSuscripcion
                        WHERE CodigoPlan = 'PREMIUM'
                        LIMIT 1
                    )
                ) AS UsuariosPremium,
                (SELECT COUNT(*) FROM UsuarioRol ur
                    INNER JOIN Rol r ON r.PK_RolID = ur.FK_RolID
                    WHERE r.NombreRol = 'Gratuito'
                ) AS UsuariosGratuitos,
                (SELECT COUNT(*) FROM Relato) AS TotalRelatosCreados,
                (SELECT COUNT(*) FROM MensajeChat
                    WHERE Emisor = 'Usuario'
                      AND MONTH(FechaEnvio) = MONTH(CURRENT_DATE())
                      AND YEAR(FechaEnvio) = YEAR(CURRENT_DATE())
                ) AS SolicitudesIAMesActual;
            """;

    private static final String SQL_PLAN_SUMMARY = """
            SELECT
                p.PK_PlanID,
                p.CodigoPlan,
                p.NombrePlan,
                p.AlmacenamientoMaxMB,
                p.Precio,
                p.ColorHex,
                p.FK_ModeloPreferidoID,
                p.Activo,
                m.NombreModelo AS ModeloPreferidoNombre,
                m.Version AS ModeloPreferidoVersion,
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
                COUNT(DISTINCT CASE
                    WHEN s.Estado = 'Activa' AND u.Activo = TRUE
                    THEN s.FK_UsuarioID
                END) AS UsuariosActivos
            FROM PlanSuscripcion p
            LEFT JOIN ModeloIA m ON m.PK_ModeloID = p.FK_ModeloPreferidoID
            LEFT JOIN Suscripcion s ON s.FK_PlanID = p.PK_PlanID AND s.Estado = 'Activa'
            LEFT JOIN Usuario u ON u.PK_UsuarioID = s.FK_UsuarioID
            GROUP BY p.PK_PlanID, p.CodigoPlan, p.NombrePlan, p.AlmacenamientoMaxMB, p.Precio, p.ColorHex,
                     p.FK_ModeloPreferidoID, p.Activo, m.NombreModelo, m.Version
            ORDER BY p.Precio ASC, p.PK_PlanID ASC;
            """;

    private static final String SQL_PAYMENT_HISTORY = """
            SELECT
                pg.PK_PagoID,
                COALESCE(pg.ReferenciaExterna, CONCAT('PAGO-', pg.PK_PagoID)) AS Referencia,
                u.Nombre,
                u.Correo,
                pg.Monto,
                pg.FechaPago,
                pg.EstadoPago
            FROM Pago pg
            INNER JOIN Suscripcion s ON s.PK_SuscripcionID = pg.FK_SuscripcionID
            INNER JOIN Usuario u ON u.PK_UsuarioID = s.FK_UsuarioID
            ORDER BY pg.FechaPago DESC, pg.PK_PagoID DESC;
            """;

    private static final String SQL_MODERATION_LOGS = """
            SELECT
                lm.PK_LogID,
                lm.Motivo,
                lm.ContenidoBloqueadoHash,
                lm.Fecha,
                u.PK_UsuarioID,
                u.Nombre,
                u.Correo
            FROM LogModeracion lm
            INNER JOIN Usuario u ON u.PK_UsuarioID = lm.FK_UsuarioID
            ORDER BY lm.Fecha DESC, lm.PK_LogID DESC;
            """;

    private static final class PlanRecord {
        private final int id;
        private final String code;
        private final String name;
        private final BigDecimal price;
        private final boolean active;

        private PlanRecord(int id, String code, String name, BigDecimal price, boolean active) {
            this.id = id;
            this.code = code == null ? "" : code.trim();
            this.name = name == null ? "" : name.trim();
            this.price = price == null ? BigDecimal.ZERO : price;
            this.active = active;
        }
    }

    /**
     * Lista usuarios con rol, plan y estado de suscripción para gestión admin.
     */
    public static JsonArray listAdminUsers() {
        JsonArray users = new JsonArray();

        try (
                Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_SELECT_ADMIN_USERS)) {

            while (rs.next()) {
                JsonObject user = new JsonObject();
                user.addProperty("PK_UsuarioID", rs.getInt("PK_UsuarioID"));
                user.addProperty("Nombre", rs.getString("Nombre"));
                user.addProperty("Correo", rs.getString("Correo"));
                user.addProperty("FotoPerfil", rs.getString("FotoPerfil") == null ? "" : rs.getString("FotoPerfil"));
                user.addProperty("Activo", rs.getBoolean("Activo"));
                user.addProperty("FechaRegistro", rs.getTimestamp("FechaRegistro").toString());
                user.addProperty("Rol", rs.getString("NombreRol"));
                user.addProperty("FK_PlanID", rs.getObject("PK_PlanID") == null ? 0 : rs.getInt("PK_PlanID"));
                user.addProperty("CodigoPlan", rs.getString("CodigoPlan") == null ? "" : rs.getString("CodigoPlan"));
                user.addProperty("NombrePlan", rs.getString("NombrePlan") == null ? "Sin plan" : rs.getString("NombrePlan"));
                user.addProperty(
                        "EstadoSuscripcion",
                        rs.getString("EstadoSuscripcion") == null ? "Sin suscripción" : rs.getString("EstadoSuscripcion"));
                users.add(user);
            }
        } catch (SQLException e) {
            JsonObject error = new JsonObject();
            error.addProperty("Mensaje", "Error al cargar usuarios administrables");
            error.addProperty("status", 500);
            users.add(error);
        }

        return users;
    }

    /**
     * Actualiza el estado activo del usuario.
     */
    public static JsonObject updateUserStatus(int userId, boolean active) {
        JsonObject response = new JsonObject();

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_USER_STATUS)) {

            stmt.setBoolean(1, active);
            stmt.setInt(2, userId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                response.addProperty("Mensaje", "Usuario no encontrado");
                response.addProperty("status", 404);
                return response;
            }

            response.addProperty("Mensaje", active ? "Usuario reactivado" : "Usuario desactivado");
            response.addProperty("status", 200);
            return response;
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al actualizar el estado del usuario");
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Reemplaza el rol actual de un usuario y registra auditoría del cambio.
     */
    public static JsonObject updateUserRole(int userId, int adminId, String newRole) {
        JsonObject response = new JsonObject();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String currentRole = null;
            Integer currentRoleId = null;
            try (PreparedStatement selectStmt = conn.prepareStatement(SQL_SELECT_USER_ROLE)) {
                selectStmt.setInt(1, userId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        currentRoleId = rs.getInt("PK_RolID");
                        currentRole = rs.getString("NombreRol");
                    }
                }
            }

            if (currentRole == null) {
                conn.rollback();
                response.addProperty("Mensaje", "Usuario no encontrado o sin rol asignado");
                response.addProperty("status", 404);
                return response;
            }

            if (currentRole.equalsIgnoreCase(newRole)) {
                conn.rollback();
                response.addProperty("Mensaje", "El usuario ya tiene ese rol");
                response.addProperty("status", 400);
                return response;
            }

            try (PreparedStatement deleteStmt = conn.prepareStatement(SQL_DELETE_USER_ROLES)) {
                deleteStmt.setInt(1, userId);
                deleteStmt.executeUpdate();
            }

            int insertedRole;
            try (PreparedStatement assignStmt = conn.prepareStatement(SQL_ASSIGN_ROLE)) {
                assignStmt.setInt(1, userId);
                assignStmt.setString(2, newRole);
                insertedRole = assignStmt.executeUpdate();
            }

            if (insertedRole == 0) {
                conn.rollback();
                response.addProperty("Mensaje", "Rol no válido");
                response.addProperty("status", 400);
                return response;
            }

            Integer newRoleId = findRoleIdByName(conn, newRole);
            if (newRoleId == null) {
                conn.rollback();
                response.addProperty("Mensaje", "Rol no válido");
                response.addProperty("status", 400);
                return response;
            }

            try (PreparedStatement auditStmt = conn.prepareStatement(SQL_INSERT_ROLE_AUDIT)) {
                auditStmt.setInt(1, userId);
                auditStmt.setInt(2, adminId);
                if (currentRoleId == null) {
                    auditStmt.setNull(3, java.sql.Types.INTEGER);
                } else {
                    auditStmt.setInt(3, currentRoleId);
                }
                auditStmt.setInt(4, newRoleId);
                auditStmt.executeUpdate();
            }

            conn.commit();
            response.addProperty("Mensaje", "Rol actualizado correctamente");
            response.addProperty("status", 200);
            return response;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                }
            }
            response.addProperty("Mensaje", "Error al actualizar el rol del usuario");
            response.addProperty("status", 500);
            return response;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeError) {
                }
            }
        }
    }

    private static Integer findRoleIdByName(Connection conn, String roleName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ROLE_ID)) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("PK_RolID");
            }
        }

        return null;
    }

    /**
     * Obtiene métricas agregadas para el dashboard administrativo.
     */
    public static JsonObject getSystemStats() {
        JsonObject stats = new JsonObject();

        try (
                Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_SYSTEM_STATS)) {

            if (!rs.next()) {
                stats.addProperty("Mensaje", "No fue posible obtener estadísticas");
                stats.addProperty("status", 404);
                return stats;
            }

            stats.addProperty("totalUsuarios", rs.getInt("TotalUsuarios"));
            stats.addProperty("usuariosActivos", rs.getInt("UsuariosActivos"));
            stats.addProperty("usuariosSuspendidos", rs.getInt("UsuariosSuspendidos"));
            stats.addProperty("usuariosPremium", rs.getInt("UsuariosPremium"));
            stats.addProperty("usuariosGratuitos", rs.getInt("UsuariosGratuitos"));
            stats.addProperty("totalRelatosCreados", rs.getInt("TotalRelatosCreados"));
            stats.addProperty("solicitudesIAMesActual", rs.getInt("SolicitudesIAMesActual"));
            stats.addProperty("status", 200);
            return stats;
        } catch (SQLException e) {
            stats.addProperty("Mensaje", "Error al obtener estadísticas del sistema");
            stats.addProperty("status", 500);
            return stats;
        }
    }

    /**
     * Resume los planes existentes con usuarios activos asociados.
     */
    public static JsonArray getPlansSummary() {
        JsonArray plans = new JsonArray();

        SettingsDao.ensureDefaultPlans();

        try (
                Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_PLAN_SUMMARY)) {

            while (rs.next()) {
                JsonObject plan = new JsonObject();
                String nombrePlan = rs.getString("NombrePlan");
                long almacenamientoMaxMb = rs.getLong("AlmacenamientoMaxMB");
                if (rs.wasNull() && nombrePlan != null && nombrePlan.toLowerCase().contains("premium")) {
                    almacenamientoMaxMb = 2048L;
                }
                plan.addProperty("PK_PlanID", rs.getInt("PK_PlanID"));
                plan.addProperty("CodigoPlan", rs.getString("CodigoPlan"));
                plan.addProperty("NombrePlan", nombrePlan);
                plan.addProperty("AlmacenamientoMaxMB", almacenamientoMaxMb);
                plan.addProperty("Precio", rs.getBigDecimal("Precio"));
                plan.addProperty("ColorHex", rs.getString("ColorHex") == null ? "" : rs.getString("ColorHex"));
                if (rs.getObject("FK_ModeloPreferidoID") != null) {
                    plan.addProperty("FK_ModeloPreferidoID", rs.getInt("FK_ModeloPreferidoID"));
                } else {
                    plan.addProperty("FK_ModeloPreferidoID", 0);
                }
                plan.addProperty(
                        "ModeloPreferidoNombre",
                        rs.getString("ModeloPreferidoNombre") == null ? "" : rs.getString("ModeloPreferidoNombre"));
                plan.addProperty(
                        "ModeloPreferidoVersion",
                        rs.getString("ModeloPreferidoVersion") == null ? "" : rs.getString("ModeloPreferidoVersion"));
                plan.addProperty("Activo", rs.getBoolean("Activo"));
                plan.addProperty("RolBase", rs.getString("RolBase"));
                plan.addProperty("UsuariosActivos", rs.getInt("UsuariosActivos"));
                plans.add(plan);
            }
        } catch (SQLException e) {
            JsonObject error = new JsonObject();
            error.addProperty("Mensaje", "Error al obtener planes");
            error.addProperty("status", 500);
            plans.add(error);
        }

        return plans;
    }

    /**
     * Crea un plan administrable y deja su rol base asociado en PlanRol.
     */
    public static JsonObject createPlan(String code, String name, Long storageMb, BigDecimal price, String colorHex,
            Integer preferredModelId, boolean active, String roleBase) {
        JsonObject response = new JsonObject();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            JsonObject preferredModelValidation = validatePreferredModel(conn, preferredModelId, roleBase);
            if (preferredModelValidation != null) {
                conn.rollback();
                return preferredModelValidation;
            }

            int planId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    """
                        INSERT INTO PlanSuscripcion(
                            CodigoPlan, NombrePlan, AlmacenamientoMaxMB, Precio, ColorHex, FK_ModeloPreferidoID, Activo
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, code);
                stmt.setString(2, name);
                if (storageMb == null) {
                    stmt.setNull(3, java.sql.Types.BIGINT);
                } else {
                    stmt.setLong(3, storageMb);
                }
                stmt.setBigDecimal(4, price);
                if (colorHex == null || colorHex.isBlank()) {
                    stmt.setNull(5, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(5, colorHex);
                }
                if (preferredModelId == null) {
                    stmt.setNull(6, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(6, preferredModelId);
                }
                stmt.setBoolean(7, active);
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No fue posible recuperar el id del plan");
                    }
                    planId = keys.getInt(1);
                }
            }

            replacePlanRole(conn, planId, roleBase);
            conn.commit();

            JsonObject plan = getPlanSummaryById(planId);
            if (plan.has("status") && plan.get("status").getAsInt() == 200) {
                plan.addProperty("Mensaje", "Plan creado correctamente");
            }
            return plan;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            response.addProperty("Mensaje",
                    isIntegrityViolation(e)
                            ? "Ya existe un plan con ese código o nombre"
                            : "Error al crear el plan");
            response.addProperty("status", isIntegrityViolation(e) ? 409 : 500);
            return response;
        } finally {
            closeTransactionalConnection(conn);
        }
    }

    /**
     * Actualiza el catálogo base de un plan existente.
     */
    public static JsonObject updatePlan(int planId, String code, String name, Long storageMb, BigDecimal price,
            String colorHex, Integer preferredModelId, boolean active, String roleBase) {
        JsonObject response = new JsonObject();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            if (!planExists(conn, planId)) {
                conn.rollback();
                response.addProperty("Mensaje", "Plan no encontrado");
                response.addProperty("status", 404);
                return response;
            }

            JsonObject preferredModelValidation = validatePreferredModel(conn, preferredModelId, roleBase);
            if (preferredModelValidation != null) {
                conn.rollback();
                return preferredModelValidation;
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    """
                        UPDATE PlanSuscripcion
                        SET CodigoPlan = ?, NombrePlan = ?, AlmacenamientoMaxMB = ?, Precio = ?,
                            ColorHex = ?, FK_ModeloPreferidoID = ?, Activo = ?
                        WHERE PK_PlanID = ?
                    """)) {
                stmt.setString(1, code);
                stmt.setString(2, name);
                if (storageMb == null) {
                    stmt.setNull(3, java.sql.Types.BIGINT);
                } else {
                    stmt.setLong(3, storageMb);
                }
                stmt.setBigDecimal(4, price);
                if (colorHex == null || colorHex.isBlank()) {
                    stmt.setNull(5, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(5, colorHex);
                }
                if (preferredModelId == null) {
                    stmt.setNull(6, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(6, preferredModelId);
                }
                stmt.setBoolean(7, active);
                stmt.setInt(8, planId);
                stmt.executeUpdate();
            }

            replacePlanRole(conn, planId, roleBase);
            conn.commit();

            JsonObject plan = getPlanSummaryById(planId);
            if (plan.has("status") && plan.get("status").getAsInt() == 200) {
                plan.addProperty("Mensaje", "Plan actualizado correctamente");
            }
            return plan;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            response.addProperty("Mensaje",
                    isIntegrityViolation(e)
                            ? "Ya existe otro plan con ese código o nombre"
                            : "Error al actualizar el plan");
            response.addProperty("status", isIntegrityViolation(e) ? 409 : 500);
            return response;
        } finally {
            closeTransactionalConnection(conn);
        }
    }

    /**
     * Elimina un plan administrable cuando no tiene historial asociado y no es base.
     */
    public static JsonObject deletePlan(int planId) {
        JsonObject response = new JsonObject();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            PlanRecord plan = findPlanById(conn, planId);
            if (plan == null) {
                conn.rollback();
                response.addProperty("Mensaje", "Plan no encontrado");
                response.addProperty("status", 404);
                return response;
            }

            if (PLAN_FREE_CODE.equalsIgnoreCase(plan.code) || PLAN_PREMIUM_CODE.equalsIgnoreCase(plan.code)) {
                conn.rollback();
                response.addProperty("Mensaje", "Los planes base del sistema no se eliminan. Puedes dejarlos inactivos.");
                response.addProperty("status", 400);
                return response;
            }

            if (planHasSubscriptions(conn, planId)) {
                conn.rollback();
                response.addProperty("Mensaje", "No puedes eliminar un plan que ya tiene suscripciones asociadas.");
                response.addProperty("status", 409);
                return response;
            }

            try (PreparedStatement deletePlanRole = conn.prepareStatement("DELETE FROM PlanRol WHERE FK_PlanID = ?");
                 PreparedStatement deletePlan = conn.prepareStatement("DELETE FROM PlanSuscripcion WHERE PK_PlanID = ?")) {
                deletePlanRole.setInt(1, planId);
                deletePlanRole.executeUpdate();

                deletePlan.setInt(1, planId);
                int affectedRows = deletePlan.executeUpdate();
                if (affectedRows <= 0) {
                    conn.rollback();
                    response.addProperty("Mensaje", "Plan no encontrado");
                    response.addProperty("status", 404);
                    return response;
                }
            }

            conn.commit();
            response.addProperty("Mensaje", "Plan eliminado correctamente");
            response.addProperty("status", 200);
            return response;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            response.addProperty("Mensaje", "Error al eliminar el plan");
            response.addProperty("status", 500);
            return response;
        } finally {
            closeTransactionalConnection(conn);
        }
    }

    /**
     * Crea o reemplaza la suscripción activa de un usuario desde el panel admin.
     */
    public static JsonObject updateUserSubscription(int userId, Integer planId, boolean cancelSubscription) {
        JsonObject response = new JsonObject();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String currentRole = null;
            try (PreparedStatement selectStmt = conn.prepareStatement(SQL_SELECT_USER_ROLE)) {
                selectStmt.setInt(1, userId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        currentRole = rs.getString("NombreRol");
                    }
                }
            }

            if (currentRole == null) {
                conn.rollback();
                response.addProperty("Mensaje", "Usuario no encontrado o sin rol asignado");
                response.addProperty("status", 404);
                return response;
            }

            if ("Admin".equalsIgnoreCase(currentRole)) {
                conn.rollback();
                response.addProperty("Mensaje", "Las cuentas Admin no se gestionan desde suscripciones");
                response.addProperty("status", 400);
                return response;
            }

            cancelActiveSubscriptions(conn, userId);

            if (cancelSubscription || planId == null || planId <= 0) {
                replaceUserRole(conn, userId, ROLE_FREE);
                conn.commit();
                response.addProperty("Mensaje", "Suscripción cancelada. El usuario quedó en acceso Gratuito.");
                response.addProperty("status", 200);
                return response;
            }

            PlanRecord plan = findPlanById(conn, planId);
            if (plan == null) {
                conn.rollback();
                response.addProperty("Mensaje", "Plan no encontrado");
                response.addProperty("status", 404);
                return response;
            }

            if (!plan.active) {
                conn.rollback();
                response.addProperty("Mensaje", "El plan seleccionado está inactivo");
                response.addProperty("status", 400);
                return response;
            }

            String roleBase = resolvePlanRole(conn, plan.id, plan.code);
            int subscriptionId = createSubscription(conn, userId, plan.id);

            if (plan.price.compareTo(BigDecimal.ZERO) > 0) {
                createAdminManagedPayment(conn, subscriptionId, plan.price);
            }

            replaceUserRole(conn, userId, roleBase);
            conn.commit();

            JsonObject subscription = SettingsDao.getSuscripcionActiva(userId);
            if (subscription.has("status") && subscription.get("status").getAsInt() == 200) {
                subscription.addProperty("Mensaje", "Suscripción actualizada correctamente");
                subscription.addProperty("rol", roleBase);
            }
            return subscription;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            response.addProperty("Mensaje", "Error al actualizar la suscripción del usuario");
            response.addProperty("status", 500);
            return response;
        } finally {
            closeTransactionalConnection(conn);
        }
    }

    /**
     * Obtiene el historial de pagos simulados ordenado por fecha.
     */
    public static JsonArray getPaymentHistory() {
        JsonArray payments = new JsonArray();

        try (
                Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_PAYMENT_HISTORY)) {

            while (rs.next()) {
                JsonObject payment = new JsonObject();
                payment.addProperty("PK_PagoID", rs.getInt("PK_PagoID"));
                payment.addProperty("Referencia", rs.getString("Referencia"));
                payment.addProperty("Nombre", rs.getString("Nombre"));
                payment.addProperty("Correo", rs.getString("Correo"));
                payment.addProperty("Monto", rs.getBigDecimal("Monto"));
                payment.addProperty("FechaPago", rs.getTimestamp("FechaPago").toString());
                payment.addProperty("EstadoPago", rs.getString("EstadoPago"));
                payment.addProperty("Simulado", (rs.getString("Referencia") != null
                        && rs.getString("Referencia").startsWith("SIM-")));
                payments.add(payment);
            }
        } catch (SQLException e) {
            JsonObject error = new JsonObject();
            error.addProperty("Mensaje", "Error al obtener historial de pagos");
            error.addProperty("status", 500);
            payments.add(error);
        }

        return payments;
    }

    /**
     * Obtiene el historial de bloqueos por moderación para auditoría admin.
     */
    public static JsonArray getModerationLogs() {
        JsonArray logs = new JsonArray();

        try (
                Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_MODERATION_LOGS)) {

            while (rs.next()) {
                JsonObject log = new JsonObject();
                log.addProperty("PK_LogID", rs.getInt("PK_LogID"));
                log.addProperty("Motivo", rs.getString("Motivo"));
                log.addProperty(
                        "ContenidoBloqueadoHash",
                        rs.getString("ContenidoBloqueadoHash") == null ? "" : rs.getString("ContenidoBloqueadoHash"));
                log.addProperty("Fecha", rs.getTimestamp("Fecha").toString());
                log.addProperty("PK_UsuarioID", rs.getInt("PK_UsuarioID"));
                log.addProperty("Nombre", rs.getString("Nombre"));
                log.addProperty("Correo", rs.getString("Correo"));
                logs.add(log);
            }
        } catch (SQLException e) {
            JsonObject error = new JsonObject();
            error.addProperty("Mensaje", "Error al obtener el historial de moderación");
            error.addProperty("status", 500);
            logs.add(error);
        }

        return logs;
    }

    private static JsonObject getPlanSummaryById(int planId) {
        JsonObject response = new JsonObject();
        String sql = """
            SELECT
                p.PK_PlanID,
                p.CodigoPlan,
                p.NombrePlan,
                p.AlmacenamientoMaxMB,
                p.Precio,
                p.ColorHex,
                p.FK_ModeloPreferidoID,
                p.Activo,
                m.NombreModelo AS ModeloPreferidoNombre,
                m.Version AS ModeloPreferidoVersion,
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
                COUNT(DISTINCT CASE
                    WHEN s.Estado = 'Activa' AND u.Activo = TRUE
                    THEN s.FK_UsuarioID
                END) AS UsuariosActivos
            FROM PlanSuscripcion p
            LEFT JOIN ModeloIA m ON m.PK_ModeloID = p.FK_ModeloPreferidoID
            LEFT JOIN Suscripcion s ON s.FK_PlanID = p.PK_PlanID AND s.Estado = 'Activa'
            LEFT JOIN Usuario u ON u.PK_UsuarioID = s.FK_UsuarioID
            WHERE p.PK_PlanID = ?
            GROUP BY p.PK_PlanID, p.CodigoPlan, p.NombrePlan, p.AlmacenamientoMaxMB, p.Precio, p.ColorHex,
                     p.FK_ModeloPreferidoID, p.Activo, m.NombreModelo, m.Version
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    response.addProperty("Mensaje", "Plan no encontrado");
                    response.addProperty("status", 404);
                    return response;
                }

                String nombrePlan = rs.getString("NombrePlan");
                long almacenamientoMaxMb = rs.getLong("AlmacenamientoMaxMB");
                if (rs.wasNull() && nombrePlan != null && nombrePlan.toLowerCase().contains("premium")) {
                    almacenamientoMaxMb = 2048L;
                }

                response.addProperty("PK_PlanID", rs.getInt("PK_PlanID"));
                response.addProperty("CodigoPlan", rs.getString("CodigoPlan"));
                response.addProperty("NombrePlan", nombrePlan);
                response.addProperty("AlmacenamientoMaxMB", almacenamientoMaxMb);
                response.addProperty("Precio", rs.getBigDecimal("Precio"));
                response.addProperty("ColorHex", rs.getString("ColorHex") == null ? "" : rs.getString("ColorHex"));
                if (rs.getObject("FK_ModeloPreferidoID") != null) {
                    response.addProperty("FK_ModeloPreferidoID", rs.getInt("FK_ModeloPreferidoID"));
                } else {
                    response.addProperty("FK_ModeloPreferidoID", 0);
                }
                response.addProperty(
                        "ModeloPreferidoNombre",
                        rs.getString("ModeloPreferidoNombre") == null ? "" : rs.getString("ModeloPreferidoNombre"));
                response.addProperty(
                        "ModeloPreferidoVersion",
                        rs.getString("ModeloPreferidoVersion") == null ? "" : rs.getString("ModeloPreferidoVersion"));
                response.addProperty("Activo", rs.getBoolean("Activo"));
                response.addProperty("RolBase", rs.getString("RolBase"));
                response.addProperty("UsuariosActivos", rs.getInt("UsuariosActivos"));
                response.addProperty("status", 200);
                return response;
            }
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener el plan");
            response.addProperty("status", 500);
            return response;
        }
    }

    private static boolean planExists(Connection conn, int planId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM PlanSuscripcion WHERE PK_PlanID = ? LIMIT 1")) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private static PlanRecord findPlanById(Connection conn, int planId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                """
                    SELECT PK_PlanID, CodigoPlan, NombrePlan, Precio, Activo
                    FROM PlanSuscripcion
                    WHERE PK_PlanID = ?
                    LIMIT 1
                """)) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PlanRecord(
                        rs.getInt("PK_PlanID"),
                        rs.getString("CodigoPlan"),
                        rs.getString("NombrePlan"),
                        rs.getBigDecimal("Precio"),
                        rs.getBoolean("Activo"));
            }
        }

        return null;
    }

    private static boolean planHasSubscriptions(Connection conn, int planId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM Suscripcion WHERE FK_PlanID = ? LIMIT 1")) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private static JsonObject validatePreferredModel(Connection conn, Integer preferredModelId, String roleBase)
            throws SQLException {
        if (preferredModelId == null) {
            return null;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                """
                    SELECT PK_ModeloID, Estado, EsGratuito
                    FROM ModeloIA
                    WHERE PK_ModeloID = ?
                    LIMIT 1
                """)) {
            stmt.setInt(1, preferredModelId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    JsonObject response = new JsonObject();
                    response.addProperty("Mensaje", "El modelo IA seleccionado no existe");
                    response.addProperty("status", 404);
                    return response;
                }

                if (!"Activo".equalsIgnoreCase(rs.getString("Estado"))) {
                    JsonObject response = new JsonObject();
                    response.addProperty("Mensaje", "El modelo IA seleccionado no está activo");
                    response.addProperty("status", 400);
                    return response;
                }

                boolean freeModel = rs.getBoolean("EsGratuito");
                if ("Gratuito".equalsIgnoreCase(roleBase) && !freeModel) {
                    JsonObject response = new JsonObject();
                    response.addProperty("Mensaje", "Un plan con rol base Gratuito solo puede usar modelos gratuitos");
                    response.addProperty("status", 400);
                    return response;
                }
            }
        }

        return null;
    }

    private static void replacePlanRole(Connection conn, int planId, String roleName) throws SQLException {
        Integer roleId = findRoleIdByName(conn, roleName);
        if (roleId == null) {
            throw new SQLException("Rol base no válido");
        }

        try (PreparedStatement deleteStmt = conn.prepareStatement(
                "DELETE FROM PlanRol WHERE FK_PlanID = ?");
             PreparedStatement insertStmt = conn.prepareStatement(
                     "INSERT INTO PlanRol(FK_PlanID, FK_RolID) VALUES (?, ?)")) {
            deleteStmt.setInt(1, planId);
            deleteStmt.executeUpdate();

            insertStmt.setInt(1, planId);
            insertStmt.setInt(2, roleId);
            insertStmt.executeUpdate();
        }
    }

    /**
     * Reemplaza el rol actual del usuario por uno nuevo dentro de la misma transacción.
     */
    private static void replaceUserRole(Connection conn, int userId, String roleName) throws SQLException {
        Integer roleId = findRoleIdByName(conn, roleName);
        if (roleId == null) {
            throw new SQLException("Rol no válido para el usuario");
        }

        try (PreparedStatement deleteStmt = conn.prepareStatement(SQL_DELETE_USER_ROLES);
             PreparedStatement insertStmt = conn.prepareStatement(
                     "INSERT INTO UsuarioRol(FK_UsuarioID, FK_RolID) VALUES (?, ?)")) {
            deleteStmt.setInt(1, userId);
            deleteStmt.executeUpdate();

            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, roleId);
            insertStmt.executeUpdate();
        }
    }

    private static void cancelActiveSubscriptions(Connection conn, int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                """
                    UPDATE Suscripcion
                    SET Estado = 'Cancelada',
                        FechaFin = COALESCE(FechaFin, CURRENT_TIMESTAMP)
                    WHERE FK_UsuarioID = ? AND Estado = 'Activa'
                """)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private static int createSubscription(Connection conn, int userId, int planId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                """
                    INSERT INTO Suscripcion(FK_UsuarioID, FK_PlanID, FechaInicio, FechaFin, Estado, RenovacionAutomatica)
                    VALUES (?, ?, CURRENT_TIMESTAMP, NULL, 'Activa', FALSE)
                """,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, planId);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("No fue posible crear la suscripción");
    }

    private static void createAdminManagedPayment(Connection conn, int subscriptionId, BigDecimal amount)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                """
                    INSERT INTO Pago(FK_SuscripcionID, Pasarela, EstadoPago, ReferenciaExterna, Monto, FechaPago)
                    VALUES (?, 'Otra', 'Completado', ?, ?, CURRENT_TIMESTAMP)
                """)) {
            stmt.setInt(1, subscriptionId);
            stmt.setString(2, "ADM-" + subscriptionId + "-" + System.currentTimeMillis());
            stmt.setBigDecimal(3, amount);
            stmt.executeUpdate();
        }
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

        if ("PREMIUM".equalsIgnoreCase(planCode)) {
            return ROLE_PREMIUM;
        }

        return ROLE_FREE;
    }

    private static boolean isIntegrityViolation(SQLException error) {
        return error != null && "23000".equals(error.getSQLState());
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private static void closeTransactionalConnection(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException ignored) {
        }
    }
}
