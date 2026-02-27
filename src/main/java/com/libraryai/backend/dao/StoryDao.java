package com.libraryai.backend.dao;

import java.sql.*;
import java.time.LocalDateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;
import com.libraryai.backend.models.Story;

/**
 * DAO para operaciones de relatos.
 */
public class StoryDao {

    // Query para insertar un nuevo relato
    static String SQL_INSERT = """
            INSERT INTO Relato (FK_UsuarioID, FK_EstanteriaID, FK_ModeloUsadoID, Titulo, ModoOrigen, Descripcion, FechaCreacion, FechaModificacion) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    // Query para obtener relatos por usuario
    static String SQL_SELECT_BY_USER = """
            SELECT PK_RelatoID, FK_UsuarioID, FK_EstanteriaID, FK_ModeloUsadoID, 
                   Titulo, ModoOrigen, Descripcion, FechaCreacion, FechaModificacion
            FROM Relato 
            WHERE FK_UsuarioID = ? 
            ORDER BY FechaCreacion DESC
            """;

    // Query para obtener un relato por ID
    static String SQL_SELECT_BY_ID = """
            SELECT PK_RelatoID, FK_UsuarioID, FK_EstanteriaID, FK_ModeloUsadoID, 
                   Titulo, ModoOrigen, Descripcion, FechaCreacion, FechaModificacion
            FROM Relato 
            WHERE PK_RelatoID = ?
            """;

    // Query para actualizar un relato
    static String SQL_UPDATE = """
            UPDATE Relato 
            SET FK_EstanteriaID = ?, FK_ModeloUsadoID = ?, Titulo = ?, 
                ModoOrigen = ?, Descripcion = ?, FechaModificacion = ?
            WHERE PK_RelatoID = ?
            """;

    // Query para eliminar un relato
    static String SQL_DELETE = """
            DELETE FROM Relato WHERE PK_RelatoID = ?
            """;

    // Query para contar relatos por usuario
    static String SQL_COUNT_BY_USER = """
            SELECT COUNT(*) FROM Relato WHERE FK_UsuarioID = ?
            """;

    /**
     * Crea un nuevo relato en la base de datos.
     * 
     * @param story Objeto Story con los datos del relato
     * @return JsonObject con el resultado de la operación
     */
    public static JsonObject create(Story story) {
        JsonObject response = new JsonObject();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, story.getUserId());
            
            if (story.getShelfId() != null) {
                pstmt.setInt(2, story.getShelfId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            if (story.getUsedModelId() != null) {
                pstmt.setInt(3, story.getUsedModelId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, story.getTitle());
            pstmt.setString(5, story.getOriginMode());
            pstmt.setString(6, story.getDescription());
            pstmt.setTimestamp(7, Timestamp.valueOf(story.getCreatedAt()));
            pstmt.setTimestamp(8, Timestamp.valueOf(story.getUpdatedAt()));
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    response.addProperty("id", idGenerado);
                    response.addProperty("Mensaje", "Relato creado correctamente");
                    response.addProperty("status", 201);
                }
            } else {
                response.addProperty("Mensaje", "No se pudo crear el relato");
                response.addProperty("status", 400);
            }
            
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al crear relato: " + e.getMessage());
            response.addProperty("status", 500);
        }
        
        return response;
    }

    /**
     * Obtiene todos los relatos de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return JsonObject con la lista de relatos
     */
    public static JsonObject findByUserId(int usuarioId) {
        JsonObject response = new JsonObject();
        JsonArray relatos = new JsonArray();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_USER)) {
            
            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                JsonObject relato = new JsonObject();
                relato.addProperty("id", rs.getInt("PK_RelatoID"));
                relato.addProperty("usuarioId", rs.getInt("FK_UsuarioID"));
                
                int estanteriaId = rs.getInt("FK_EstanteriaID");
                if (!rs.wasNull()) {
                    relato.addProperty("estanteriaId", estanteriaId);
                }
                
                int modeloUsadoId = rs.getInt("FK_ModeloUsadoID");
                if (!rs.wasNull()) {
                    relato.addProperty("modeloUsadoId", modeloUsadoId);
                }
                
                relato.addProperty("titulo", rs.getString("Titulo"));
                relato.addProperty("modoOrigen", rs.getString("ModoOrigen"));
                relato.addProperty("descripcion", rs.getString("Descripcion"));
                relato.addProperty("fechaCreacion", rs.getTimestamp("FechaCreacion").toString());
                relato.addProperty("fechaModificacion", rs.getTimestamp("FechaModificacion").toString());
                
                relatos.add(relato);
            }
            
            response.add("relatos", relatos);
            response.addProperty("total", relatos.size());
            response.addProperty("status", 200);
            
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener relatos: " + e.getMessage());
            response.addProperty("status", 500);
        }
        
        return response;
    }

    /**
     * Obtiene un relato por su ID.
     * 
     * @param relatoId ID del relato
     * @return JsonObject con los datos del relato
     */
    public static JsonObject findById(int relatoId) {
        JsonObject response = new JsonObject();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            pstmt.setInt(1, relatoId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JsonObject relato = new JsonObject();
                relato.addProperty("id", rs.getInt("PK_RelatoID"));
                relato.addProperty("usuarioId", rs.getInt("FK_UsuarioID"));
                
                int estanteriaId = rs.getInt("FK_EstanteriaID");
                if (!rs.wasNull()) {
                    relato.addProperty("estanteriaId", estanteriaId);
                }
                
                int modeloUsadoId = rs.getInt("FK_ModeloUsadoID");
                if (!rs.wasNull()) {
                    relato.addProperty("modeloUsadoId", modeloUsadoId);
                }
                
                relato.addProperty("titulo", rs.getString("Titulo"));
                relato.addProperty("modoOrigen", rs.getString("ModoOrigen"));
                relato.addProperty("descripcion", rs.getString("Descripcion"));
                relato.addProperty("fechaCreacion", rs.getTimestamp("FechaCreacion").toString());
                relato.addProperty("fechaModificacion", rs.getTimestamp("FechaModificacion").toString());
                
                response.add("relato", relato);
                response.addProperty("status", 200);
            } else {
                response.addProperty("Mensaje", "Relato no encontrado");
                response.addProperty("status", 404);
            }
            
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener relato: " + e.getMessage());
            response.addProperty("status", 500);
        }
        
        return response;
    }

    /**
     * Actualiza un relato existente.
     * 
     * @param story Objeto Story con los datos actualizados
     * @return JsonObject con el resultado de la operación
     */
    public static JsonObject update(Story story) {
        JsonObject response = new JsonObject();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE)) {
            
            if (story.getShelfId() != null) {
                pstmt.setInt(1, story.getShelfId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            
            if (story.getUsedModelId() != null) {
                pstmt.setInt(2, story.getUsedModelId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            pstmt.setString(3, story.getTitle());
            pstmt.setString(4, story.getOriginMode());
            pstmt.setString(5, story.getDescription());
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(7, story.getStoryId());
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                response.addProperty("Mensaje", "Relato actualizado correctamente");
                response.addProperty("status", 200);
            } else {
                response.addProperty("Mensaje", "No se pudo actualizar el relato o no existe");
                response.addProperty("status", 400);
            }
            
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al actualizar relato: " + e.getMessage());
            response.addProperty("status", 500);
        }
        
        return response;
    }

    /**
     * Elimina un relato por su ID.
     * 
     * @param relatoId ID del relato a eliminar
     * @return JsonObject con el resultado de la operación
     */
    public static JsonObject delete(int relatoId) {
        JsonObject response = new JsonObject();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE)) {
            
            pstmt.setInt(1, relatoId);
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                response.addProperty("Mensaje", "Relato eliminado correctamente");
                response.addProperty("status", 200);
            } else {
                response.addProperty("Mensaje", "No se pudo eliminar el relato o no existe");
                response.addProperty("status", 400);
            }
            
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al eliminar relato: " + e.getMessage());
            response.addProperty("status", 500);
        }
        
        return response;
    }

    /**
     * Cuenta el número de relatos de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Número de relatos del usuario
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
}
