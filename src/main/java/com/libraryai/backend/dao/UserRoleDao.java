package com.libraryai.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.libraryai.backend.config.DbConnection;

/**
 * DAO para relacion usuario-rol.
 */
public class UserRoleDao {
    private static final int ROL_USUARIO_DEFAULT = 2;

    // language=sql
    private static final String SQL_INSERT_ROL = """
            INSERT INTO UsuarioRol(FK_UsuarioID, FK_RolID) VALUES(?, ?);
            """;
    
    /**
     * Asigna el rol por defecto a un usuario recien creado.
     */
    public static void assignRole(int id){
        try (
            Connection conn = DbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_ROL);
        ) {
            // FK_UsuarioID y FK_RolID.
            pstmt.setInt(1, id);
            pstmt.setInt(2, ROL_USUARIO_DEFAULT);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
}
