package com.libraryai.backend.seeders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.libraryai.backend.config.ConexionDB;

public class SeedRoles {

    // language=sql
    private static String SQL_INSERT = """
            INSERT INTO Rol(NombreRol) VALUES(?);
            """;

    private static String[] roles = new String[] {
            "Admin", "Gratuito", "Premium"
    };

    public static void insertRoles() {
        try (
                Connection conn = ConexionDB.getConexion();
                PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT);
            ) {

            // Verificar si ya existen roles
            String checkSQL = "SELECT COUNT(*) FROM Rol";

            try (
                PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
                ResultSet rs = checkStmt.executeQuery()
                ) {
                
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Los roles ya existen en la base de datos");
                    return;
                }
            }
            
            // Insertar roles
            int filasAfect = 0;
            for (String rol : roles) {
                pstmt.setString(1, rol);
                filasAfect += pstmt.executeUpdate();
            }

            System.out.println("Total de roles insertados: " + filasAfect);

        } catch (SQLException e) {
            System.err.println("Error al insertar los roles, e: " + e.getMessage());
        }
    }
}