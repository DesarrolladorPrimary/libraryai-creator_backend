package com.libraryai.backend.seeders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;

import com.libraryai.backend.config.AIConfig;
import com.libraryai.backend.config.DatabaseConnection;

/**
 * Sincroniza el catálogo mínimo de modelos IA del sistema.
 *
 * <p>No asume una base vacía: si el modelo ya existe lo actualiza y si falta lo
 * inserta, manteniendo coherencia con la configuración actual de entorno.
 */
public class SeedAIModels {

    private static final String DEFAULT_FREE_MODEL = "gemini-2.5-flash";
    private static final String DEFAULT_PREMIUM_MODEL = "gemini-2.5-pro";
    private static final String SQL_FIND_BY_NAME = """
            SELECT PK_ModeloID
            FROM ModeloIA
            WHERE LOWER(NombreModelo) = LOWER(?)
            LIMIT 1
            """;

    private static final String SQL_INSERT = """
            INSERT INTO ModeloIA (
                NombreModelo, Version, Descripcion, NotasVersion, FechaLanzamiento, EsGratuito, Estado
            ) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, 'Activo')
            """;

    private static final String SQL_UPDATE = """
            UPDATE ModeloIA
            SET Version = ?, Descripcion = ?, NotasVersion = ?, FechaLanzamiento = CURRENT_TIMESTAMP,
                EsGratuito = ?, Estado = 'Activo'
            WHERE PK_ModeloID = ?
            """;

    /**
     * Garantiza que existan al menos un modelo gratuito y uno premium para que el
     * backend pueda resolver disponibilidad por plan desde el primer arranque.
     */
    public static void insertModels() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String freeModel = normalizeModelName(AIConfig.FREE_MODEL);
            if (freeModel.isBlank()) {
                freeModel = DEFAULT_FREE_MODEL;
            }

            ensureModel(
                    conn,
                    freeModel,
                    true,
                    "Modelo base disponible para todos los usuarios.",
                    "Modelo recomendado para el plan Gratuito.");

            String premiumModel = normalizeModelName(AIConfig.PREMIUM_MODEL);
            if (premiumModel.isBlank()) {
                premiumModel = DEFAULT_PREMIUM_MODEL;
            }

            if (!premiumModel.equalsIgnoreCase(freeModel)) {
                ensureModel(
                        conn,
                        premiumModel,
                        false,
                        "Modelo avanzado habilitado para usuarios Premium.",
                        "Modelo preferente para el plan Premium.");
            }

            System.out.println("Catálogo mínimo de modelos IA sincronizado correctamente");
        } catch (SQLException e) {
            System.err.println("Error al insertar modelos IA: " + e.getMessage());
        }
    }

    /**
     * Inserta o actualiza un modelo puntual del catálogo base.
     */
    private static void ensureModel(Connection conn, String modelName, boolean isFreeTier, String description,
            String releaseNotes) throws SQLException {
        Integer existingId = findModelIdByName(conn, modelName);
        if (existingId != null) {
            updateModel(conn, existingId, modelName, isFreeTier, description, releaseNotes);
            return;
        }

        insertModel(conn, modelName, isFreeTier, description, releaseNotes);
    }

    /**
     * Busca coincidencias por nombre sin depender de mayúsculas/minúsculas.
     */
    private static Integer findModelIdByName(Connection conn, String modelName) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            pstmt.setString(1, modelName);

            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("PK_ModeloID");
            }
        }

        return null;
    }

    /**
     * Inserta un modelo nuevo con la metadata mínima visible para el usuario y el
     * panel admin.
     */
    private static void insertModel(Connection conn, String modelName, boolean isFreeTier, String description,
            String releaseNotes) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
            pstmt.setString(1, modelName);
            pstmt.setString(2, buildVersion(modelName));
            pstmt.setString(3, description);
            pstmt.setString(4, releaseNotes);
            pstmt.setBoolean(5, isFreeTier);
            pstmt.executeUpdate();
        }
    }

    /**
     * Mantiene sincronizado un modelo ya existente con la configuración esperada
     * por el backend.
     */
    private static void updateModel(Connection conn, int modelId, String modelName, boolean isFreeTier, String description,
            String releaseNotes) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE)) {
            pstmt.setString(1, buildVersion(modelName));
            pstmt.setString(2, description);
            pstmt.setString(3, releaseNotes);
            pstmt.setBoolean(4, isFreeTier);
            pstmt.setInt(5, modelId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Limpia espacios y evita nulls antes de persistir nombres de modelos.
     */
    private static String normalizeModelName(String modelName) {
        return String.valueOf(modelName == null ? "" : modelName).trim();
    }

    /**
     * Deriva una versión legible a partir del nombre del modelo para poblar el
     * catálogo sin depender de otro origen externo.
     */
    private static String buildVersion(String modelName) {
        String normalized = normalizeModelName(modelName);
        if (normalized.startsWith("gemini-")) {
            return normalized.substring("gemini-".length());
        }

        return normalized.isBlank() ? "sin-version" : normalized;
    }
}
