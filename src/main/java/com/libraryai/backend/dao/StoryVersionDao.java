package com.libraryai.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO para versionado de relatos.
 */
public class StoryVersionDao {

    private static final String SQL_NEXT_VERSION = """
            SELECT COALESCE(MAX(NumeroVersion), 0) + 1 AS nextVersion
            FROM RelatoVersion
            WHERE FK_RelatoID = ?
            """;

    private static final String SQL_INSERT = """
            INSERT INTO RelatoVersion (FK_RelatoID, NumeroVersion, Contenido, Notas, EsPublicada)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SQL_DELETE_BY_STORY = """
            DELETE FROM RelatoVersion
            WHERE FK_RelatoID = ?
            """;

    public static JsonObject createVersion(int storyId, String content, String notes, boolean published) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection()) {
            int nextVersion = getNextVersion(conn, storyId);

            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
                pstmt.setInt(1, storyId);
                pstmt.setInt(2, nextVersion);
                pstmt.setString(3, content);
                pstmt.setString(4, notes);
                pstmt.setBoolean(5, published);
                pstmt.executeUpdate();
            }

            response.addProperty("version", nextVersion);
            response.addProperty("Mensaje", "Versión del relato guardada correctamente");
            response.addProperty("status", 201);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al guardar versión del relato: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Elimina todas las versiones asociadas a un relato.
     */
    public static JsonObject deleteByStory(int storyId) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_BY_STORY)) {

            pstmt.setInt(1, storyId);
            int deletedRows = pstmt.executeUpdate();

            response.addProperty("filasAfectadas", deletedRows);
            response.addProperty("Mensaje", deletedRows > 0
                    ? "Versiones del relato eliminadas correctamente"
                    : "No había versiones asociadas al relato");
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al eliminar versiones del relato: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    private static int getNextVersion(Connection conn, int storyId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_NEXT_VERSION)) {
            pstmt.setInt(1, storyId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("nextVersion");
            }
        }

        return 1;
    }
}
