package com.libraryai.backend.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.libraryai.backend.config.ConexionDB;
import com.libraryai.backend.models.Usuario;

//**Clase encargada de servir de puente entre el modelo y la DB
public class UsuariosDao {
    // **Consulta SQL

    // Language=sql
    private final static String SQL_INSERT = """
            INSERT INTO usuario(Nombre, Correo, PasswordHash, FechaRegistro, Activo) values (?,?,?,?,?)
            """;

    

    // **Metodo encargado de crear la conexion, recibir los datos del modelo y
    // realizar la insercion para crear un usuario
    public static void registrarUsuario(Usuario usuario) {

        try (
                Connection conn = ConexionDB.getConexion();
                PreparedStatement sfmt = conn.prepareStatement(SQL_INSERT);) {

            sfmt.setString(1, usuario.getNombre());
            sfmt.setString(2, usuario.getCorreo());
            sfmt.setString(3, usuario.getContrasenaHash());
            sfmt.setDate(4, Date.valueOf(usuario.getFechaRegistro()));
            sfmt.setBoolean(5, usuario.isActivo());

            int filasAfectadas = sfmt.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("Usuario añadido a la DB");
            }

        } catch (SQLException e) {
            System.err.println("Error al registrar el usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
