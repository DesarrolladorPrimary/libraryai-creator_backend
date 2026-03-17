package com.libraryai.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO para configuraciones de IA asociadas a un relato.
 *
 * <p>Permite leer, crear, actualizar y eliminar los ajustes editoriales que
 * modulan el comportamiento de la IA sobre un relato concreto.
 */
public class AIConfigurationDao {

    private static final String SQL_SELECT_BY_STORY = """
            SELECT PK_ConfigID, FK_RelatoID, EstiloEscritura, NivelCreatividad,
                   LongitudRespuesta, TonoEmocional
            FROM ConfiguracionIA
            WHERE FK_RelatoID = ?
            """;

    private static final String SQL_UPSERT = """
            INSERT INTO ConfiguracionIA (
                FK_RelatoID, EstiloEscritura, NivelCreatividad, LongitudRespuesta, TonoEmocional
            ) VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                EstiloEscritura = VALUES(EstiloEscritura),
                NivelCreatividad = VALUES(NivelCreatividad),
                LongitudRespuesta = VALUES(LongitudRespuesta),
                TonoEmocional = VALUES(TonoEmocional)
            """;

    private static final String SQL_DELETE_BY_STORY = """
            DELETE FROM ConfiguracionIA
            WHERE FK_RelatoID = ?
            """;

    /**
     * Recupera la configuración IA actualmente asociada a un relato.
     */
    public static JsonObject findByStoryId(int storyId) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_STORY)) {

            pstmt.setInt(1, storyId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                response.addProperty("Mensaje", "Configuración de IA no encontrada");
                response.addProperty("status", 404);
                return response;
            }

            JsonObject config = new JsonObject();
            config.addProperty("id", rs.getInt("PK_ConfigID"));
            config.addProperty("relatoId", rs.getInt("FK_RelatoID"));
            config.addProperty("estiloEscritura", rs.getString("EstiloEscritura"));
            config.addProperty("nivelCreatividad", rs.getString("NivelCreatividad"));
            config.addProperty("longitudRespuesta", rs.getString("LongitudRespuesta"));
            config.addProperty("tonoEmocional", rs.getString("TonoEmocional"));

            response.add("configuracionIA", config);
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener configuración de IA: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Inserta o actualiza la configuración IA del relato con una sola operación.
     */
    public static JsonObject upsert(int storyId, String writingStyle, String creativityLevel,
            String responseLength, String emotionalTone) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_UPSERT)) {

            pstmt.setInt(1, storyId);
            pstmt.setString(2, writingStyle);
            pstmt.setString(3, creativityLevel);
            pstmt.setString(4, responseLength);
            pstmt.setString(5, emotionalTone);
            pstmt.executeUpdate();

            response.addProperty("Mensaje", "Configuración de IA actualizada correctamente");
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al guardar configuración de IA: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Elimina la configuración de IA asociada a un relato.
     */
    public static JsonObject deleteByStory(int storyId) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_BY_STORY)) {

            pstmt.setInt(1, storyId);
            int deletedRows = pstmt.executeUpdate();

            response.addProperty("filasAfectadas", deletedRows);
            response.addProperty("Mensaje", deletedRows > 0
                    ? "Configuración de IA eliminada correctamente"
                    : "No había configuración de IA asociada al relato");
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al eliminar configuración de IA: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }
}
