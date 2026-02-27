package com.libraryai.backend.dao.auth;

import java.sql.*;
import java.time.LocalDateTime;

import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO para operaciones de recuperación de contraseña.
 */
public class RecuperacionDao {

    // Query para guardar un nuevo token (el tipo se pasa como parámetro)
    static String SQL_INSERT = """
            INSERT INTO TokenAcceso (FK_UsuarioID, TipoToken, Token, FechaExpiracion) 
            VALUES (?, ?, ?, ?)
            """;

    // Query para buscar token de recuperación
    static String SQL_SELECT_TOKEN_RECUPERACION = """
            SELECT PK_TokenID, FK_UsuarioID, Token, FechaExpiracion, Usado 
            FROM TokenAcceso 
            WHERE Token = ? AND TipoToken = 'Recuperacion_Password' AND Usado = FALSE
            """;
    
    // Query para buscar token de verificación
    static String SQL_SELECT_TOKEN_VERIFICACION = """
            SELECT PK_TokenID, FK_UsuarioID, Token, FechaExpiracion, Usado 
            FROM TokenAcceso 
            WHERE Token = ? AND TipoToken = 'Verificacion_Registro' AND Usado = FALSE
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
     * Guarda un token en la base de datos.
     * 
     * @param usuarioId ID del usuario.
     * @param token Token único.
     * @param expiracion Fecha y hora cuando expira el token.
     * @param tipoToken Tipo de token (Verificacion_Registro o Recuperacion_Password).
     */
    public static void guardarToken(int usuarioId, String token, LocalDateTime expiracion, String tipoToken) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
            
            pstmt.setInt(1, usuarioId);
            pstmt.setString(2, tipoToken);
            pstmt.setString(3, token);
            pstmt.setTimestamp(4, Timestamp.valueOf(expiracion));
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al guardar token: " + e.getMessage());
        }
    }

    /**
     * Guarda un token de recuperación (para compatibilidad).
     */
    public static void guardarToken(int usuarioId, String token, LocalDateTime expiracion) {
        guardarToken(usuarioId, token, expiracion, "Recuperacion_Password");
    }

    /**
     * Valida un token de recuperación de contraseña.
     */
    public static JsonObject validarToken(String token) {
        return validarTokenPorTipo(token, "Recuperacion_Password");
    }
    
    /**
     * Valida un token de verificación de registro.
     */
    public static JsonObject validarTokenVerificacion(String token) {
        return validarTokenPorTipo(token, "Verificacion_Registro");
    }
    
    /**
     * Valida un token según su tipo.
     */
    private static JsonObject validarTokenPorTipo(String token, String tipoToken) {
        JsonObject response = new JsonObject();
        
        String sql = tipoToken.equals("Verificacion_Registro") 
            ? SQL_SELECT_TOKEN_VERIFICACION 
            : SQL_SELECT_TOKEN_RECUPERACION;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                LocalDateTime expiracion = rs.getTimestamp("FechaExpiracion").toLocalDateTime();
                
                if (expiracion.isBefore(LocalDateTime.now())) {
                    response.addProperty("status", 400);
                    response.addProperty("Mensaje", "Token expirado");
                } else {
                    response.addProperty("status", 200);
                    response.addProperty("usuarioId", rs.getInt("FK_UsuarioID"));
                    response.addProperty("tokenId", rs.getInt("PK_TokenID"));
                }
            } else {
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
