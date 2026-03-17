package com.libraryai.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO para relacion usuario-rol.
 */
public class UserRoleDao {
    private static final String DEFAULT_ROLE_NAME = "Gratuito";

    // language=sql
    private static final String SQL_INSERT_ROL = """
            INSERT INTO UsuarioRol(FK_UsuarioID, FK_RolID)
            SELECT ?, PK_RolID
            FROM Rol
            WHERE NombreRol = ?
            LIMIT 1;
            """;
    
    /**
     * Asigna el rol por defecto a un usuario recien creado.
     */
    public static boolean assignRole(int id){
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_ROL);
        ) {
            pstmt.setInt(1, id);
            pstmt.setString(2, DEFAULT_ROLE_NAME);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }
    
}
