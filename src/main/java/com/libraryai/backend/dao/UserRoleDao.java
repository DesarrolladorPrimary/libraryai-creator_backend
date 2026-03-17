package com.libraryai.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO para la relación usuario-rol.
 *
 * <p>Actualmente se usa sobre todo durante el registro para asignar el rol base
 * del sistema a cuentas recién creadas.
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
     * Asigna el rol Gratuito a un usuario recién creado.
     *
     * @return {@code true} cuando la inserción encontró el rol y pudo persistir la
     *         relación.
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
