package com.libraryai.backend.dao.story;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;
import com.libraryai.backend.models.Story;

/**
 * DAO para operaciones de relatos y su relación con estanterías.
 */
public class StoryDao {

    private static final String SQL_INSERT = """
            INSERT INTO Relato (FK_UsuarioID, FK_ModeloUsadoID, Titulo, ModoOrigen, Descripcion, FechaCreacion, FechaModificacion)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_SELECT_BY_USER = """
            SELECT PK_RelatoID, FK_UsuarioID, FK_ModeloUsadoID,
                   Titulo, ModoOrigen, Descripcion, FechaCreacion, FechaModificacion
            FROM Relato
            WHERE FK_UsuarioID = ?
            ORDER BY FechaCreacion DESC
            """;

    private static final String SQL_SELECT_BY_ID = """
            SELECT PK_RelatoID, FK_UsuarioID, FK_ModeloUsadoID,
                   Titulo, ModoOrigen, Descripcion, FechaCreacion, FechaModificacion
            FROM Relato
            WHERE PK_RelatoID = ?
            """;

    private static final String SQL_UPDATE = """
            UPDATE Relato
            SET FK_ModeloUsadoID = ?, Titulo = ?, ModoOrigen = ?, Descripcion = ?, FechaModificacion = ?
            WHERE PK_RelatoID = ?
            """;

    private static final String SQL_UPDATE_DESCRIPTION = """
            UPDATE Relato
            SET Descripcion = ?, FechaModificacion = ?
            WHERE PK_RelatoID = ?
            """;

    private static final String SQL_DELETE = """
            DELETE FROM Relato WHERE PK_RelatoID = ?
            """;

    private static final String SQL_DELETE_SHELF_LINKS = """
            DELETE FROM Relato_Estanteria
            WHERE FK_RelatoID = ?
            """;

    private static final String SQL_INSERT_SHELF_LINK = """
            INSERT INTO Relato_Estanteria (FK_RelatoID, FK_EstanteriaID)
            VALUES (?, ?)
            """;

    private static final String SQL_SELECT_SHELVES_BY_STORY = """
            SELECT e.PK_EstanteriaID, e.NombreCategoria
            FROM Relato_Estanteria re
            JOIN Estanteria e ON e.PK_EstanteriaID = re.FK_EstanteriaID
            WHERE re.FK_RelatoID = ?
            ORDER BY e.NombreCategoria, e.PK_EstanteriaID
            """;

    private static final String SQL_COUNT_BY_USER = """
            SELECT COUNT(*) FROM Relato WHERE FK_UsuarioID = ?
            """;

    /**
     * Crea un nuevo relato en la base de datos junto con sus estanterías.
     */
    public static JsonObject create(Story story) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, story.getUserId());

                if (story.getUsedModelId() != null) {
                    pstmt.setInt(2, story.getUsedModelId());
                } else {
                    pstmt.setNull(2, java.sql.Types.INTEGER);
                }

                pstmt.setString(3, story.getTitle());
                pstmt.setString(4, story.getOriginMode());
                pstmt.setString(5, story.getDescription());
                pstmt.setTimestamp(6, Timestamp.valueOf(story.getCreatedAt()));
                pstmt.setTimestamp(7, Timestamp.valueOf(story.getUpdatedAt()));

                int filasAfectadas = pstmt.executeUpdate();
                if (filasAfectadas <= 0) {
                    conn.rollback();
                    response.addProperty("Mensaje", "No se pudo crear el relato");
                    response.addProperty("status", 400);
                    return response;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        conn.rollback();
                        response.addProperty("Mensaje", "No se pudo obtener el ID del relato creado");
                        response.addProperty("status", 500);
                        return response;
                    }

                    int storyId = generatedKeys.getInt(1);
                    replaceShelves(conn, storyId, story.getShelfIds());
                    conn.commit();

                    response.addProperty("id", storyId);
                    response.addProperty("Mensaje", "Relato creado correctamente");
                    response.addProperty("status", 201);
                    return response;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al crear relato: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Obtiene todos los relatos de un usuario.
     */
    public static JsonObject findByUserId(int usuarioId) {
        JsonObject response = new JsonObject();
        JsonArray relatos = new JsonArray();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_USER)) {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JsonObject relato = mapStoryRow(rs);
                attachShelves(conn, rs.getInt("PK_RelatoID"), relato);
                relatos.add(relato);
            }

            response.add("relatos", relatos);
            response.addProperty("total", relatos.size());
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener relatos: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Obtiene un relato por su ID.
     */
    public static JsonObject findById(int relatoId) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            pstmt.setInt(1, relatoId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                response.addProperty("Mensaje", "Relato no encontrado");
                response.addProperty("status", 404);
                return response;
            }

            JsonObject relato = mapStoryRow(rs);
            attachShelves(conn, relatoId, relato);

            response.add("relato", relato);
            response.addProperty("status", 200);
            return response;

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener relato: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Actualiza un relato y reemplaza su conjunto de estanterías.
     */
    public static JsonObject update(Story story) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE)) {
                if (story.getUsedModelId() != null) {
                    pstmt.setInt(1, story.getUsedModelId());
                } else {
                    pstmt.setNull(1, java.sql.Types.INTEGER);
                }

                pstmt.setString(2, story.getTitle());
                pstmt.setString(3, story.getOriginMode());
                pstmt.setString(4, story.getDescription());
                pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(6, story.getStoryId());

                int filasAfectadas = pstmt.executeUpdate();
                if (filasAfectadas <= 0) {
                    conn.rollback();
                    response.addProperty("Mensaje", "No se pudo actualizar el relato o no existe");
                    response.addProperty("status", 400);
                    return response;
                }

                replaceShelves(conn, story.getStoryId(), story.getShelfIds());
                conn.commit();

                response.addProperty("Mensaje", "Relato actualizado correctamente");
                response.addProperty("status", 200);
                return response;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al actualizar relato: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Actualiza únicamente el contenido del borrador del relato.
     */
    public static JsonObject updateDescription(int relatoId, String descripcion) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_DESCRIPTION)) {

            pstmt.setString(1, descripcion != null ? descripcion.trim() : "");
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, relatoId);

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                response.addProperty("Mensaje", "Borrador del relato actualizado correctamente");
                response.addProperty("status", 200);
            } else {
                response.addProperty("Mensaje", "No se pudo actualizar el borrador del relato");
                response.addProperty("status", 400);
            }

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al actualizar borrador del relato: " + e.getMessage());
            response.addProperty("status", 500);
        }

        return response;
    }

    /**
     * Elimina un relato por su ID junto con su relación a estanterías.
     */
    public static JsonObject delete(int relatoId) {
        JsonObject response = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteLinks = conn.prepareStatement(SQL_DELETE_SHELF_LINKS);
                    PreparedStatement deleteStory = conn.prepareStatement(SQL_DELETE)) {

                deleteLinks.setInt(1, relatoId);
                deleteLinks.executeUpdate();

                deleteStory.setInt(1, relatoId);
                int filasAfectadas = deleteStory.executeUpdate();

                if (filasAfectadas > 0) {
                    conn.commit();
                    response.addProperty("Mensaje", "Relato eliminado correctamente");
                    response.addProperty("status", 200);
                } else {
                    conn.rollback();
                    response.addProperty("Mensaje", "No se pudo eliminar el relato o no existe");
                    response.addProperty("status", 400);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al eliminar relato: " + e.getMessage());
            response.addProperty("status", 500);
        }

        return response;
    }

    /**
     * Cuenta el número de relatos de un usuario.
     */
    public static int countByUserId(int usuarioId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_COUNT_BY_USER)) {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al contar relatos: " + e.getMessage());
        }

        return 0;
    }

    private static JsonObject mapStoryRow(ResultSet rs) throws SQLException {
        JsonObject relato = new JsonObject();
        relato.addProperty("id", rs.getInt("PK_RelatoID"));
        relato.addProperty("usuarioId", rs.getInt("FK_UsuarioID"));

        int modeloUsadoId = rs.getInt("FK_ModeloUsadoID");
        if (!rs.wasNull()) {
            relato.addProperty("modeloUsadoId", modeloUsadoId);
        }

        relato.addProperty("titulo", rs.getString("Titulo"));
        relato.addProperty("modoOrigen", rs.getString("ModoOrigen"));
        relato.addProperty("descripcion", rs.getString("Descripcion"));
        relato.addProperty("fechaCreacion", rs.getTimestamp("FechaCreacion").toString());

        Timestamp updatedAt = rs.getTimestamp("FechaModificacion");
        if (updatedAt != null) {
            relato.addProperty("fechaModificacion", updatedAt.toString());
        }

        return relato;
    }

    private static void attachShelves(Connection conn, int storyId, JsonObject relato) throws SQLException {
        JsonArray shelfIds = new JsonArray();
        JsonArray shelves = new JsonArray();

        try (PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_SHELVES_BY_STORY)) {
            pstmt.setInt(1, storyId);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder shelfNames = new StringBuilder();
            Integer firstShelfId = null;
            String firstShelfName = null;

            while (rs.next()) {
                int shelfId = rs.getInt("PK_EstanteriaID");
                String shelfName = rs.getString("NombreCategoria");

                shelfIds.add(shelfId);

                JsonObject shelf = new JsonObject();
                shelf.addProperty("id", shelfId);
                shelf.addProperty("nombre", shelfName);
                shelves.add(shelf);

                if (firstShelfId == null) {
                    firstShelfId = shelfId;
                    firstShelfName = shelfName;
                }

                if (shelfNames.length() > 0) {
                    shelfNames.append(", ");
                }
                shelfNames.append(shelfName);
            }

            relato.add("estanteriaIds", shelfIds);
            relato.add("estanterias", shelves);

            if (firstShelfId != null) {
                relato.addProperty("estanteriaId", firstShelfId);
            }
            if (firstShelfName != null && !firstShelfName.isBlank()) {
                relato.addProperty("nombreEstanteria", firstShelfName);
            }
            if (shelfNames.length() > 0) {
                relato.addProperty("nombresEstanterias", shelfNames.toString());
            }
        }
    }

    private static void replaceShelves(Connection conn, int storyId, List<Integer> shelfIds) throws SQLException {
        try (PreparedStatement deleteStmt = conn.prepareStatement(SQL_DELETE_SHELF_LINKS)) {
            deleteStmt.setInt(1, storyId);
            deleteStmt.executeUpdate();
        }

        if (shelfIds == null || shelfIds.isEmpty()) {
            return;
        }

        try (PreparedStatement insertStmt = conn.prepareStatement(SQL_INSERT_SHELF_LINK)) {
            for (Integer shelfId : new LinkedHashSet<>(shelfIds)) {
                if (shelfId == null || shelfId <= 0) {
                    continue;
                }

                insertStmt.setInt(1, storyId);
                insertStmt.setInt(2, shelfId);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
        }
    }
}
