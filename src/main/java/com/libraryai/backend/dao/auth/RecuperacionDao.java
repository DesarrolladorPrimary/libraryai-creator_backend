package com.libraryai.backend.dao.auth;

import java.sql.*;
import java.time.LocalDateTime;

import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO para operaciones de recuperación de contraseña.
 */
public class RecuperacionDao {

    // Query para guardar un nuevo token de recuperación
    static String SQL_INSERT = """
            INSERT INTO TokenAcceso (FK_UsuarioID, TipoToken, Token, FechaExpiracion) 
            VALUES (?, 'recuperacion', ?, ?)
            """;

    // Query para buscar y validar un token
    static String SQL_SELECT_TOKEN = """
            SELECT PK_TokenID, FK_UsuarioID, Token, FechaExpiracion, Usado 
            FROM TokenAcceso 
            WHERE Token = ? AND TipoToken = 'recuperacion' AND Usado = FALSE
            """;

    // Query para marcar un token como usado
    static String SQL_UPDATE_USADO = """
            UPDATE TokenAcceso SET Usado = TRUE WHERE PK_TokenID = ?
            """;

    // Query para actualizar la contraseña del usuario
    static String SQL_UPDATE_PASSWORD = """
            UPDATE Usuario SET PasswordHash = ? WHERE PK_UsuarioID = ?
            """;

    /**
     * Guarda un token de recuperación en la base de datos.
     * 
     * @param usuarioId ID del usuario que solicita recuperación.
     * @param token Token único de recuperación.
     * @param expiracion Fecha y hora cuando expira el token.
     */
    public static void guardarToken(int usuarioId, String token, LocalDateTime expiracion) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
            
            // Asigna los parámetros a la query
            pstmt.setInt(1, usuarioId);
            pstmt.setString(2, token);
            pstmt.setTimestamp(3, Timestamp.valueOf(expiracion));
            
            // Ejecuta la inserción
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al guardar token: " + e.getMessage());
        }
    }

    /**
     * Valida un token de recuperación.
     * Verifica que exista, no haya sido usado y no haya expirado.
     * 
     * @param token Token a validar.
     * @return JsonObject con status 200 si es válido, o error si es inválido/expirado.
     */
    public static JsonObject validarToken(String token) {
        // Objeto para la respuesta
        JsonObject response = new JsonObject();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_TOKEN)) {
            
            // Asigna el token a la query
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            
            // Si encuentra el token
            if (rs.next()) {
                // Obtiene la fecha de expiración
                LocalDateTime expiracion = rs.getTimestamp("FechaExpiracion").toLocalDateTime();
                
                // Verifica si el token ha expirado
                if (expiracion.isBefore(LocalDateTime.now())) {
                    response.addProperty("status", 400);
                    response.addProperty("Mensaje", "Token expirado");
                } else {
                    // Token válido, retorna los datos necesarios
                    response.addProperty("status", 200);
                    response.addProperty("usuarioId", rs.getInt("FK_UsuarioID"));
                    response.addProperty("tokenId", rs.getInt("PK_TokenID"));
                }
            } else {
                // Token no encontrado o ya usado
                response.addProperty("status", 404);
                response.addProperty("Mensaje", "Token inválido o ya usado");
            }
            
        } catch (SQLException e) {
            response.addProperty("status", 500);
            response.addProperty("Mensaje", "Error del servidor");
        }
        
        return response;
    }

    /**
     * Marca un token como usado para que no se pueda reutilizar.
     * 
     * @param tokenId ID del token a marcar.
     */
    public static void marcarTokenUsado(int tokenId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_USADO)) {
            
            pstmt.setInt(1, tokenId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al marcar token: " + e.getMessage());
        }
    }

    /**
     * Actualiza la contraseña de un usuario.
     * 
     * @param usuarioId ID del usuario.
     * @param nuevoHash Nueva contraseña ya hasheada.
     * @return true si se actualizó, false si hubo error.
     */
    public static boolean actualizarPassword(int usuarioId, String nuevoHash) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_PASSWORD)) {
            
            pstmt.setString(1, nuevoHash);
            pstmt.setInt(2, usuarioId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar password: " + e.getMessage());
            return false;
        }
    }
}
