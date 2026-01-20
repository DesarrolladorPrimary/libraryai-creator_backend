package com.libraryai.backend.dao.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.libraryai.backend.config.ConexionDB;

/**
 * DAO para autenticacion de usuarios.
 */
public class LoginDao {

    // language=sql
    final static String SQL_SELECT_WHERE = """
            SELECT u.Correo, u.Passwordhash, r.NombreRol, u.PK_UsuarioID
            FROM Usuario u
            INNER JOIN UsuarioRol ur ON u.PK_UsuarioID = ur.FK_UsuarioID
            INNER JOIN Rol r ON ur.FK_RolID = r.PK_RolID
            WHERE u.Correo = ?
                        """;

    /**
     * Busca un usuario por correo y devuelve hash, rol e id.
     * Retorna status 200 si encuentra, 404 si no existe.
     */
    public static JsonObject validarUsuario(String correo) {
        JsonObject user = new JsonObject();

        try (
                Connection conn = ConexionDB.getConexion();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_WHERE);) {
            // Prepara la consulta con el correo recibido.
            pstmt.setString(1, correo);
            ResultSet rs = pstmt.executeQuery();

            // Si no hay filas, el usuario no existe.
            if (!rs.next()) {
                user.addProperty("Mensaje", "El usuario no existe");
                user.addProperty("status", 404);
            } else {
                // Lee los datos necesarios para login.
                do {
                    String contraseñaDB = rs.getString("Passwordhash");
                    String rolUsuario = rs.getString("NombreRol");
                    int idUsuario = rs.getInt("PK_UsuarioID");
                    user.addProperty("status", 200);
                    user.addProperty("contraseña", contraseñaDB);
                    user.addProperty("rol", rolUsuario);
                    user.addProperty("id", idUsuario);
                } while (rs.next());
            }

        } catch (SQLException e) {
            user.addProperty("status", 500);
            e.printStackTrace();
        }

        return user;
    }

}
