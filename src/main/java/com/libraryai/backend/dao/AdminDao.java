package com.libraryai.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO para consultas del panel de administración.
 */
public class AdminDao {

    private static final String SQL_SELECT_ADMIN_USERS = """
            SELECT
                u.PK_UsuarioID,
                u.Nombre,
                u.Correo,
                u.FotoPerfil,
                u.Activo,
                u.FechaRegistro,
                r.NombreRol,
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
            SELECT r.NombreRol
            FROM UsuarioRol ur
            INNER JOIN Rol r ON r.PK_RolID = ur.FK_RolID
            WHERE ur.FK_UsuarioID = ?
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
            INSERT INTO AuditoriaRolUsuario(FK_UsuarioAfectadoID, FK_AdminID, RolAnterior, RolNuevo)
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
                        WHERE LOWER(NombrePlan) LIKE '%premium%'
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
                p.NombrePlan,
                p.AlmacenamientoMaxMB,
                p.Precio,
                p.Activo,
                COUNT(DISTINCT CASE
                    WHEN u.Activo = TRUE
                         AND (
                            (LOWER(p.NombrePlan) LIKE '%gratuito%' AND r.NombreRol = 'Gratuito')
                            OR
                            (LOWER(p.NombrePlan) LIKE '%premium%' AND r.NombreRol = 'Premium')
                         )
                    THEN u.PK_UsuarioID
                END) AS UsuariosActivos
            FROM PlanSuscripcion p
            LEFT JOIN UsuarioRol ur ON 1 = 1
            LEFT JOIN Rol r ON r.PK_RolID = ur.FK_RolID
            LEFT JOIN Usuario u ON u.PK_UsuarioID = ur.FK_UsuarioID
            GROUP BY p.PK_PlanID, p.NombrePlan, p.AlmacenamientoMaxMB, p.Precio, p.Activo
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

            response.addProperty("Mensaje", active ? "Usuario reactivado" : "Usuario suspendido");
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

            try (PreparedStatement auditStmt = conn.prepareStatement(SQL_INSERT_ROLE_AUDIT)) {
                auditStmt.setInt(1, userId);
                auditStmt.setInt(2, adminId);
                auditStmt.setString(3, currentRole);
                auditStmt.setString(4, newRole);
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
                plan.addProperty("NombrePlan", nombrePlan);
                plan.addProperty("AlmacenamientoMaxMB", almacenamientoMaxMb);
                plan.addProperty("Precio", rs.getBigDecimal("Precio"));
                plan.addProperty("Activo", rs.getBoolean("Activo"));
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
}
