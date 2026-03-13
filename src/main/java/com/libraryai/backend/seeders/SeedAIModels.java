package com.libraryai.backend.seeders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import com.libraryai.backend.config.AIConfig;
import com.libraryai.backend.config.DatabaseConnection;

/**
 * Inserta un catálogo mínimo de modelos IA para instalaciones nuevas.
 */
public class SeedAIModels {

    private static final String DEFAULT_FREE_MODEL = "gemini-2.5-flash";

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM ModeloIA";

    private static final String SQL_INSERT = """
            INSERT INTO ModeloIA (
                NombreModelo, Version, Descripcion, NotasVersion, FechaLanzamiento, EsGratuito, Estado
            ) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, 'Activo')
            """;

    public static void insertModels() {
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement countStmt = conn.prepareStatement(SQL_COUNT);
                ResultSet rs = countStmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Los modelos IA ya existen en la base de datos");
                return;
            }

            insertModel(conn, DEFAULT_FREE_MODEL, true, "Modelo base disponible para todos los usuarios.",
                    "Semilla inicial para entornos locales.");

            String configuredModel = normalizeModelName(AIConfig.MODEL_AI);
            if (!configuredModel.isBlank() && !configuredModel.equalsIgnoreCase(DEFAULT_FREE_MODEL)) {
                insertModel(conn, configuredModel, isFreeTierModel(configuredModel),
                        "Modelo configurado desde GEMINI_MODEL.",
                        "Insertado automaticamente a partir de la configuracion del backend.");
            }

            System.out.println("Modelos IA mínimos insertados correctamente");
        } catch (SQLException e) {
            System.err.println("Error al insertar modelos IA: " + e.getMessage());
        }
    }

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

    private static String normalizeModelName(String modelName) {
        return String.valueOf(modelName == null ? "" : modelName).trim();
    }

    private static boolean isFreeTierModel(String modelName) {
        String normalized = normalizeModelName(modelName).toLowerCase(Locale.ROOT);
        return !normalized.contains("pro") && !normalized.contains("ultra");
    }

    private static String buildVersion(String modelName) {
        String normalized = normalizeModelName(modelName);
        if (normalized.startsWith("gemini-")) {
            return normalized.substring("gemini-".length());
        }

        return normalized.isBlank() ? "sin-version" : normalized;
    }
}
