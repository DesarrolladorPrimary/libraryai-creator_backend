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
 * DAO para archivos del usuario y su relación con relatos.
 */
public class UploadedFileDao {

    private static final String SQL_INSERT = """
            INSERT INTO ArchivoUsuario (
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
            JOIN ArchivoUsuario a ON raf.FK_ArchivoID = a.PK_ArchivoID
            WHERE raf.FK_RelatoID = ?
            ORDER BY a.FechaSubida DESC, a.PK_ArchivoID DESC
            """;

    private static final String SQL_DELETE_BY_USER = """
            DELETE FROM ArchivoUsuario
            WHERE PK_ArchivoID = ? AND FK_UsuarioID = ?
            """;

    private static final String SQL_SELECT_BY_STORY_AND_ORIGIN = """
            SELECT a.PK_ArchivoID, a.FK_UsuarioID, a.NombreArchivo, a.TipoArchivo,
                   a.Origen, a.RutaAlmacenamiento, a.TamanoBytes, a.FechaSubida
            FROM Relato_ArchivoFuente raf
            JOIN ArchivoUsuario a ON raf.FK_ArchivoID = a.PK_ArchivoID
            WHERE raf.FK_RelatoID = ? AND a.Origen = ?
            ORDER BY a.FechaSubida DESC, a.PK_ArchivoID DESC
            """;

    private static final String SQL_SUM_BYTES_BY_USER_AND_ORIGIN = """
            SELECT COALESCE(SUM(TamanoBytes), 0) AS totalBytes
            FROM ArchivoUsuario
            WHERE FK_UsuarioID = ? AND Origen = ?
            """;

    private static final String SQL_SUM_BYTES_BY_USER = """
            SELECT COALESCE(SUM(TamanoBytes), 0) AS totalBytes
            FROM ArchivoUsuario
            WHERE FK_UsuarioID = ?
            """;

    private static final String SQL_SELECT_EXPORTED_BY_USER = """
            SELECT a.PK_ArchivoID, a.FK_UsuarioID, a.NombreArchivo, a.TipoArchivo,
                   a.Origen, a.RutaAlmacenamiento, a.TamanoBytes, a.FechaSubida,
                   r.PK_RelatoID, r.Titulo, r.FK_EstanteriaID, e.NombreCategoria
            FROM ArchivoUsuario a
            JOIN Relato_ArchivoFuente raf ON a.PK_ArchivoID = raf.FK_ArchivoID
            JOIN Relato r ON raf.FK_RelatoID = r.PK_RelatoID
            LEFT JOIN Estanteria e ON r.FK_EstanteriaID = e.PK_EstanteriaID
            WHERE a.FK_UsuarioID = ? AND a.Origen = 'Exportado'
            """;

    private static final String SQL_SELECT_BY_ID_AND_USER_AND_ORIGIN = """
            SELECT PK_ArchivoID, FK_UsuarioID, NombreArchivo, TipoArchivo, Origen,
                   RutaAlmacenamiento, TamanoBytes, FechaSubida
            FROM ArchivoUsuario
            WHERE PK_ArchivoID = ? AND FK_UsuarioID = ? AND Origen = ?
            """;

    private static final String SQL_EXISTS_BY_USER_AND_ORIGIN_AND_NAME = """
            SELECT 1
            FROM ArchivoUsuario
            WHERE FK_UsuarioID = ? AND Origen = ?
              AND LOWER(TRIM(NombreArchivo)) = LOWER(TRIM(?))
            LIMIT 1
            """;

    private static final String SQL_EXISTS_BY_USER_AND_ORIGIN_AND_NAME_EXCLUDING_STORY = """
            SELECT 1
            FROM ArchivoUsuario a
            JOIN Relato_ArchivoFuente raf ON raf.FK_ArchivoID = a.PK_ArchivoID
            WHERE a.FK_UsuarioID = ? AND a.Origen = ?
              AND LOWER(TRIM(a.NombreArchivo)) = LOWER(TRIM(?))
              AND raf.FK_RelatoID <> ?
            LIMIT 1
            """;

    private static final String SQL_DELETE_LINK = """
            DELETE FROM Relato_ArchivoFuente
            WHERE FK_RelatoID = ? AND FK_ArchivoID = ?
            """;

    private static final String SQL_DELETE_LINKS_BY_FILE = """
            DELETE FROM Relato_ArchivoFuente
            WHERE FK_ArchivoID = ?
            """;

    private static final String SQL_COUNT_STORY_LINKS = """
            SELECT COUNT(*) AS total
            FROM Relato_ArchivoFuente
            WHERE FK_ArchivoID = ?
            """;

    /**
     * Registra metadatos de un archivo del usuario, ya sea subido o exportado.
     */
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

    /**
     * Vincula un archivo existente con un relato.
     */
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

    /**
     * Lista todos los archivos asociados a un relato.
     */
    public static JsonObject listByStory(int storyId) {
        JsonObject response = new JsonObject();
        JsonArray files = new JsonArray();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_STORY)) {

            pstmt.setInt(1, storyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                files.add(mapUploadedFile(rs));
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

    /**
     * Lista archivos de un relato filtrando por origen.
     */
    public static JsonObject listByStoryAndOrigin(int storyId, String origin) {
        JsonObject response = new JsonObject();
        JsonArray files = new JsonArray();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_STORY_AND_ORIGIN)) {

            pstmt.setInt(1, storyId);
            pstmt.setString(2, origin);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                files.add(mapUploadedFile(rs));
            }

            response.add("archivos", files);
            response.addProperty("total", files.size());
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener archivos filtrados del relato: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Suma almacenamiento consumido por usuario para un origen concreto.
     */
    public static long sumBytesByUserAndOrigin(int userId, String origin) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SUM_BYTES_BY_USER_AND_ORIGIN)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, origin);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("totalBytes");
            }
        } catch (SQLException e) {
            return 0L;
        }

        return 0L;
    }

    /**
     * Suma el almacenamiento total consumido por un usuario.
     */
    public static long sumBytesByUser(int userId) {
        if (userId <= 0) {
            return 0L;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SUM_BYTES_BY_USER)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("totalBytes");
            }
        } catch (SQLException e) {
            return 0L;
        }

        return 0L;
    }

    /**
     * Lista documentos exportados visibles en biblioteca.
     */
    public static JsonObject listExportedByUser(int userId, Integer shelfId) {
        JsonObject response = new JsonObject();
        JsonArray documents = new JsonArray();

        StringBuilder sqlBuilder = new StringBuilder(SQL_SELECT_EXPORTED_BY_USER);
        if (shelfId != null && shelfId > 0) {
            sqlBuilder.append(" AND r.FK_EstanteriaID = ?");
        }
        sqlBuilder.append(" ORDER BY a.FechaSubida DESC, a.PK_ArchivoID DESC");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            pstmt.setInt(1, userId);
            if (shelfId != null && shelfId > 0) {
                pstmt.setInt(2, shelfId);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JsonObject document = mapUploadedFile(rs);
                document.addProperty("relatoId", rs.getInt("PK_RelatoID"));
                document.addProperty("tituloRelato", rs.getString("Titulo"));

                int shelfValue = rs.getInt("FK_EstanteriaID");
                if (!rs.wasNull()) {
                    document.addProperty("estanteriaId", shelfValue);
                }

                String shelfName = rs.getString("NombreCategoria");
                if (shelfName != null && !shelfName.isBlank()) {
                    document.addProperty("nombreEstanteria", shelfName);
                }

                documents.add(document);
            }

            response.add("documentos", documents);
            response.addProperty("total", documents.size());
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener documentos exportados: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Busca un archivo por id, usuario propietario y origen esperado.
     */
    public static JsonObject findByIdAndUserAndOrigin(int fileId, int userId, String origin) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_ID_AND_USER_AND_ORIGIN)) {

            pstmt.setInt(1, fileId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, origin);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                response.add("archivo", mapUploadedFile(rs));
                response.addProperty("status", 200);
                return response;
            }

            response.addProperty("Mensaje", "Archivo no encontrado");
            response.addProperty("status", 404);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener archivo: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Valida si el usuario ya tiene un archivo registrado con el mismo nombre y
     * origen.
     */
    public static boolean existsByUserAndOriginAndName(int userId, String origin, String fileName) {
        if (userId <= 0 || origin == null || origin.isBlank() || fileName == null || fileName.isBlank()) {
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_EXISTS_BY_USER_AND_ORIGIN_AND_NAME)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, origin.trim());
            pstmt.setString(3, fileName.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Valida si ya existe un archivo con el mismo nombre para el usuario,
     * ignorando los exportados que pertenecen al relato que se está
     * reconvirtiendo.
     */
    public static boolean existsByUserAndOriginAndNameExcludingStory(int userId, String origin, String fileName,
            int storyId) {
        if (userId <= 0 || storyId <= 0 || origin == null || origin.isBlank() || fileName == null
                || fileName.isBlank()) {
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_EXISTS_BY_USER_AND_ORIGIN_AND_NAME_EXCLUDING_STORY)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, origin.trim());
            pstmt.setString(3, fileName.trim());
            pstmt.setInt(4, storyId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Elimina el vínculo entre un relato y un archivo ya registrado.
     */
    public static JsonObject unlinkFromStory(int storyId, int fileId) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_LINK)) {

            pstmt.setInt(1, storyId);
            pstmt.setInt(2, fileId);
            pstmt.executeUpdate();

            response.addProperty("Mensaje", "Archivo desvinculado del relato");
            response.addProperty("status", 200);
            return response;
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al desvincular archivo del relato: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Elimina todos los vínculos de un archivo con cualquier relato.
     */
    public static JsonObject deleteLinksByFile(int fileId) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_LINKS_BY_FILE)) {

            pstmt.setInt(1, fileId);
            int deletedRows = pstmt.executeUpdate();

            response.addProperty("filasAfectadas", deletedRows);
            response.addProperty("Mensaje", deletedRows > 0
                    ? "Vínculos del archivo eliminados correctamente"
                    : "No había vínculos asociados al archivo");
            response.addProperty("status", 200);
            return response;
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al eliminar vínculos del archivo: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Cuenta cuántos relatos siguen vinculados a un archivo.
     */
    public static int countStoryLinks(int fileId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_COUNT_STORY_LINKS)) {

            pstmt.setInt(1, fileId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            return 0;
        }

        return 0;
    }

    /**
     * Elimina un archivo siempre que pertenezca al usuario indicado.
     */
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

    private static JsonObject mapUploadedFile(ResultSet rs) throws SQLException {
        JsonObject file = new JsonObject();
        file.addProperty("id", rs.getInt("PK_ArchivoID"));
        file.addProperty("usuarioId", rs.getInt("FK_UsuarioID"));
        file.addProperty("nombreArchivo", rs.getString("NombreArchivo"));
        file.addProperty("tipoArchivo", rs.getString("TipoArchivo"));
        file.addProperty("origen", rs.getString("Origen"));
        file.addProperty("rutaAlmacenamiento", rs.getString("RutaAlmacenamiento"));
        file.addProperty("tamanoBytes", rs.getLong("TamanoBytes"));
        file.addProperty("fechaSubida", rs.getTimestamp("FechaSubida").toString());
        return file;
    }
}
