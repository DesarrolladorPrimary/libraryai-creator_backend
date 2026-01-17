package com.libraryai.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.libraryai.backend.config.ConexionDB;

public class UsuarioRolDao {
    private static final int ROL_USUARIO_DEFAULT = 2;

    // language=sql
    private static final String SQL_INSERT_ROL = """
            INSERT INTO UsuarioRol(FK_UsuarioID, FK_RolID) VALUES(?, ?);
            """;
    
    public static void asignarRol(int id){
        try (
            Connection conn = ConexionDB.getConexion();
            PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_ROL);
        ) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, ROL_USUARIO_DEFAULT);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
}
