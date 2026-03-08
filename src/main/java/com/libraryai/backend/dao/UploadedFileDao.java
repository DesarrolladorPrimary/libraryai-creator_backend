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
 * DAO para archivos subidos y su relación con relatos.
 */
public class UploadedFileDao {

    private static final String SQL_INSERT = """
            INSERT INTO ArchivoSubido (
                FK_UsuarioID, NombreArchivo, TipoArchivo, Origen, RutaAlmacenamiento, TamanoBytes
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_LINK_TO_STORY = """
            INSERT IGNORE INTO Relato_ArchivoFuente (FK_RelatoID, FK_ArchivoID)
            VALUES (?, ?)
            """;

    private static final String SQL_SELECT_BY_STORY = """
            SELECT a.PK_ArchivoID, a.FK_UsuarioID, a.NombreArchivo, a.TipoArchivo,
                   a.Origen, a.RutaAlmacenamiento, a.TamanoBytes, a.FechaSubida
            FROM Relato_ArchivoFuente raf
            JOIN ArchivoSubido a ON raf.FK_ArchivoID = a.PK_ArchivoID
            WHERE raf.FK_RelatoID = ?
            ORDER BY a.FechaSubida DESC, a.PK_ArchivoID DESC
            """;

    private static final String SQL_DELETE_BY_USER = """
            DELETE FROM ArchivoSubido
            WHERE PK_ArchivoID = ? AND FK_UsuarioID = ?
            """;

    public static JsonObject create(int userId, String fileName, String fileType, String origin,
            String storagePath, long sizeBytes) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, fileName);
            pstmt.setString(3, fileType);
            pstmt.setString(4, origin);
            pstmt.setString(5, storagePath);
            pstmt.setLong(6, sizeBytes);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows <= 0) {
                response.addProperty("Mensaje", "No se pudo registrar el archivo");
                response.addProperty("status", 400);
                return response;
            }

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                response.addProperty("id", keys.getInt(1));
            }

            response.addProperty("Mensaje", "Archivo registrado correctamente");
            response.addProperty("status", 201);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al registrar archivo: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    public static JsonObject linkToStory(int storyId, int fileId) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_LINK_TO_STORY)) {

            pstmt.setInt(1, storyId);
            pstmt.setInt(2, fileId);
            pstmt.executeUpdate();

            response.addProperty("Mensaje", "Archivo asociado correctamente al relato");
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al asociar archivo al relato: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    public static JsonObject listByStory(int storyId) {
        JsonObject response = new JsonObject();
        JsonArray files = new JsonArray();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_STORY)) {

            pstmt.setInt(1, storyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JsonObject file = new JsonObject();
                file.addProperty("id", rs.getInt("PK_ArchivoID"));
                file.addProperty("usuarioId", rs.getInt("FK_UsuarioID"));
                file.addProperty("nombreArchivo", rs.getString("NombreArchivo"));
                file.addProperty("tipoArchivo", rs.getString("TipoArchivo"));
                file.addProperty("origen", rs.getString("Origen"));
                file.addProperty("rutaAlmacenamiento", rs.getString("RutaAlmacenamiento"));
                file.addProperty("tamanoBytes", rs.getLong("TamanoBytes"));
                file.addProperty("fechaSubida", rs.getTimestamp("FechaSubida").toString());
                files.add(file);
            }

            response.add("archivos", files);
            response.addProperty("total", files.size());
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener archivos del relato: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    public static JsonObject deleteByUser(int fileId, int userId) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_BY_USER)) {

            pstmt.setInt(1, fileId);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows <= 0) {
                response.addProperty("Mensaje", "No se pudo eliminar el archivo o no existe");
                response.addProperty("status", 404);
                return response;
            }

            response.addProperty("Mensaje", "Archivo eliminado correctamente");
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al eliminar archivo: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }
}
