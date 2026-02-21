package com.libraryai.backend.dao;

import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * SETTINGSDAO - Acceso a BD para configuraciones de usuario
 *
 * Operaciones:
 * - getInstruccionIA:     Lee InstruccionPermanenteIA de la tabla Usuario
 * - updateInstruccionIA:  Actualiza InstruccionPermanenteIA en la tabla Usuario
 * - getSuscripcionActiva: Lee Suscripcion + PlanSuscripcion del usuario activo
 */
public class SettingsDao {

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
                response.addProperty("status", 200);
                response.addProperty("plan", rs.getString("NombrePlan"));
                response.addProperty("almacenamiento", rs.getInt("AlmacenamientoMaxMB"));
                response.addProperty("precio", rs.getDouble("Precio"));
                response.addProperty("estado", rs.getString("Estado"));
                response.addProperty("fechaInicio", rs.getString("FechaInicio"));
                String fechaFin = rs.getString("FechaFin");
                response.addProperty("fechaFin", fechaFin != null ? fechaFin : "Sin vencimiento");
                response.addProperty("renovacionAutomatica", rs.getBoolean("RenovacionAutomatica"));
            } else {
                // Sin suscripción activa: valores por defecto
                response.addProperty("status", 200);
                response.addProperty("plan", "Sin Plan");
                response.addProperty("almacenamiento", 0);
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
}
