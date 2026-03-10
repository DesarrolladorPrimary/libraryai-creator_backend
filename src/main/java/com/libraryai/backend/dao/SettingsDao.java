package com.libraryai.backend.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.AIConfig;
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
                String planName = rs.getString("NombrePlan");
                Long almacenamientoMax = rs.getObject("AlmacenamientoMaxMB") == null
                        ? null
                        : ((Number) rs.getObject("AlmacenamientoMaxMB")).longValue();
                boolean almacenamientoIlimitado = (planName != null && planName.toLowerCase().contains("premium"))
                        || almacenamientoMax == null
                        || almacenamientoMax <= 0;
                double usedStorageMb = bytesToMb(UploadedFileDao.sumBytesByUser(userId));
                response.addProperty("status", 200);
                response.addProperty("plan", planName);
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

    private static double bytesToMb(long bytes) {
        return Math.round((bytes / 1024d / 1024d) * 100.0d) / 100.0d;
    }

    /**
     * Obtiene la versión actual del modelo IA (RF_32).
     */
    public static JsonObject getVersionActual() {
        JsonObject response = new JsonObject();
        String sql = """
            SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, Estado, EsGratuito
            FROM ModeloIA
            WHERE Estado = 'Activo'
            ORDER BY CASE
                WHEN LOWER(NombreModelo) = LOWER(?) THEN 0
                ELSE 1
            END, PK_ModeloID ASC
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, AIConfig.MODEL_AI == null ? "" : AIConfig.MODEL_AI.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                response.addProperty("status", 200);
                response.addProperty("id", rs.getInt("PK_ModeloID"));
                response.addProperty("version", rs.getString("Version"));
                response.addProperty("nombre", rs.getString("NombreModelo"));
                response.addProperty("descripcion", rs.getString("Descripcion"));
                response.addProperty("changelog", rs.getString("NotasVersion") != null ? rs.getString("NotasVersion") : "");
                response.addProperty("activo", rs.getString("NombreModelo"));
                response.addProperty("gratuito", rs.getBoolean("EsGratuito"));
            } else {
                return buildConfiguredModelFallback();
            }

        } catch (Exception e) {
            System.out.println("Error en getVersionActual: " + e.getMessage());
            return buildConfiguredModelFallback();
        }

        return response;
    }

    /**
     * Obtiene modelos disponibles según el plan del usuario (RF_32).
     */
    public static JsonObject getModelosPorPlan(String plan) {
        JsonObject response = new JsonObject();
        
        // Según el RF, los modelos disponibles dependen del plan
        String sql;
        if ("Premium".equals(plan)) {
            // Premium tiene acceso a todos los modelos
            sql = """
                SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, EsGratuito
                FROM ModeloIA
                WHERE Estado = 'Activo'
                ORDER BY EsGratuito ASC, PK_ModeloID ASC
            """;
        } else {
            // Gratuito solo tiene acceso a modelos gratuitos
            sql = """
                SELECT PK_ModeloID, NombreModelo, Version, Descripcion, NotasVersion, EsGratuito
                FROM ModeloIA
                WHERE Estado = 'Activo' AND EsGratuito = TRUE
                ORDER BY PK_ModeloID ASC
            """;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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

    private static JsonObject buildModelJson(ResultSet rs) throws java.sql.SQLException {
        JsonObject modelo = new JsonObject();
        modelo.addProperty("id", rs.getInt("PK_ModeloID"));
        modelo.addProperty("nombre", rs.getString("NombreModelo"));
        modelo.addProperty("version", rs.getString("Version"));
        modelo.addProperty("descripcion", rs.getString("Descripcion") != null ? rs.getString("Descripcion") : "");
        modelo.addProperty("notasVersion", rs.getString("NotasVersion") != null ? rs.getString("NotasVersion") : "");
        modelo.addProperty("gratuito", rs.getBoolean("EsGratuito"));
        return modelo;
    }

    private static JsonObject buildConfiguredModelFallback() {
        JsonObject response = new JsonObject();
        String configuredModel = AIConfig.MODEL_AI == null ? "" : AIConfig.MODEL_AI.trim();

        if (configuredModel.isBlank()) {
            response.addProperty("status", 404);
            response.addProperty("Mensaje", "No hay modelo activo");
            return response;
        }

        response.addProperty("status", 200);
        response.addProperty("nombre", configuredModel);
        response.addProperty("version", "Configuracion global");
        response.addProperty("descripcion", "Modelo operativo configurado desde el entorno del backend.");
        response.addProperty("changelog", "Sin registro cargado en la tabla ModeloIA.");
        response.addProperty("activo", configuredModel);
        response.addProperty("gratuito", true);
        return response;
    }
}
