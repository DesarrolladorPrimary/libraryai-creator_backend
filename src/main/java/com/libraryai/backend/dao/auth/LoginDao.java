package com.libraryai.backend.dao.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.libraryai.backend.config.ConexionDB;

public class LoginDao {

    // language=sql
    final static String SQL_SELECT_WHERE = """
            SELECT Correo, PasswordHash FROM usuario WHERE Correo = ?;
            """;

    public static JsonObject validarUsuario(String correo) {
        JsonObject user = new JsonObject();

        try (
                Connection conn = ConexionDB.getConexion();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_WHERE);
            ) 
            {
            pstmt.setString(1, correo);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                user.addProperty("Mensaje", "El usuario no existe");
                user.addProperty("status", 404);
            }
            else{
                do {
                    int contraseñaDB = rs.getInt("Passwordhash");
                    user.addProperty("status", 200);
                    user.addProperty("contraseña", contraseñaDB);
                } while (rs.next());
            }

        } catch (SQLException e) {
            user.addProperty("status", 500);
            e.printStackTrace();
        }

        return user;
    }

}
