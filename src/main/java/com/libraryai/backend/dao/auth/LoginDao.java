package com.libraryai.backend.dao.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO de autenticacion orientado al login.
 *
 * <p>Recupera la informacion minima necesaria para validar credenciales y
 * construir el token de acceso del usuario.
 */
public class LoginDao {

    // language=sql
    final static String SQL_SELECT_WHERE = """
            SELECT u.Correo, u.Passwordhash, u.CorreoVerificado, u.Activo, r.NombreRol, u.PK_UsuarioID
            FROM Usuario u
            INNER JOIN UsuarioRol ur ON u.PK_UsuarioID = ur.FK_UsuarioID
            INNER JOIN Rol r ON ur.FK_RolID = r.PK_RolID
            WHERE u.Correo = ?
                        """;

    // language=sql
    final static String SQL_SELECT_ACTIVE_BY_ID = """
            SELECT Activo
            FROM Usuario
            WHERE PK_UsuarioID = ?
            LIMIT 1
                        """;

    /**
     * Busca un usuario por correo y devuelve hash, rol, id, estado de
     * verificacion y si la cuenta sigue activa.
     *
     * <p>La consulta usa {@code INNER JOIN} con {@code UsuarioRol} y {@code Rol},
     * así que una cuenta sin rol asignado no se considera válida para el login.
     */
    public static JsonObject findUserByEmail(String correo) {
        JsonObject user = new JsonObject();

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_WHERE)) {
            pstmt.setString(1, correo);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                user.addProperty("Mensaje", "El usuario no existe");
                user.addProperty("status", 404);
            } else {
                String passwordDb = rs.getString("Passwordhash");
                String roleName = rs.getString("NombreRol");
                int userId = rs.getInt("PK_UsuarioID");
                boolean verifiedEmail = rs.getBoolean("CorreoVerificado");
                boolean active = rs.getBoolean("Activo");

                user.addProperty("status", 200);
                user.addProperty("contrasena", passwordDb);
                user.addProperty("rol", roleName);
                user.addProperty("id", userId);
                user.addProperty("correoVerificado", verifiedEmail);
                user.addProperty("activo", active);
            }

        } catch (SQLException e) {
            user.addProperty("status", 500);
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Consulta si la cuenta del usuario sigue activa.
     *
     * <p>Se usa en revalidaciones posteriores al login cuando ya se conoce el id
     * del usuario autenticado.
     */
    public static JsonObject findUserActiveStatus(int userId) {
        JsonObject userStatus = new JsonObject();

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_ACTIVE_BY_ID)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                userStatus.addProperty("Mensaje", "El usuario no existe");
                userStatus.addProperty("status", 404);
            } else {
                userStatus.addProperty("status", 200);
                userStatus.addProperty("activo", rs.getBoolean("Activo"));
            }
        } catch (SQLException e) {
            userStatus.addProperty("Mensaje", "No fue posible validar el estado del usuario");
            userStatus.addProperty("status", 500);
            e.printStackTrace();
        }

        return userStatus;
    }
}
