package com.libraryai.backend.dao;

import java.sql.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.ConexionDB;
import com.libraryai.backend.models.Usuario;

//**Clase encargada de servir de puente entre el modelo y la DB
public class UsuariosDao {
    // **Consultas SQL

    // Language=sql
    private final static String SQL_INSERT = """
            INSERT INTO usuario(Nombre, Correo, PasswordHash, FechaRegistro, Activo) values (?,?,?,?,?)
            """;
    // language=sql
    private final static String SQL_SELECT_WHERE = """
            SELECT Correo FROM usuario WHERE Correo = ?;
            """;

    // language=sql
    private final static String SQL_SELECT = """
            SELECT Nombre, Correo, PasswordHash FROM usuario;
            """;

    /**
     * Metodo encargado de crear la conexion, recibir los datos del modelo y
     * realizar la insercion para crear un usuario en la base de datos.
     * 
     * @param usuario Objeto Usuario con los datos a insertar.
     */
    public static void registrarUsuario(Usuario usuario) {

        try (
                // Obtenemos la conexión a la base de datos desde la clase de configuración
                Connection conn = ConexionDB.getConexion();
                // Preparamos la sentencia SQL usando la constante definida arriba
                PreparedStatement sfmt = conn.prepareStatement(SQL_INSERT);) {

            // Asignamos el nombre al primer parámetro (?) del SQL
            sfmt.setString(1, usuario.getNombre());
            // Asignamos el correo al segundo parámetro
            sfmt.setString(2, usuario.getCorreo());
            // Asignamos el hash de la contraseña al tercer parámetro
            sfmt.setString(3, usuario.getContrasenaHash());
            // Convertimos la fecha LocalDate a Date de SQL y la asignamos
            sfmt.setDate(4, Date.valueOf(usuario.getFechaRegistro()));
            // Asignamos el booleano 'Activo'
            sfmt.setBoolean(5, usuario.isActivo());

            // Ejecutamos la actualización en la DB y guardamos cuántas filas se afectaron
            int filasAfectadas = sfmt.executeUpdate();

            // Si se afectó al menos una fila, significa que se guardó correctamente
            if (filasAfectadas > 0) {
                System.out.println("Usuario añadido a la DB");
            }

        } catch (SQLException e) {
            // Si hay error SQL, lo imprimimos en la consola de errores
            System.err.println("Error al registrar el usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recupera todos los usuarios de la base de datos.
     * 
     * @return JsonArray con los usuarios encontrados o un mensaje de error si no
     *         hay datos.
     */
    public static JsonArray ObtenerUsuarios() {

        // Creamos la lista JSON donde guardaremos los usuarios
        JsonArray listarUsuarios = new JsonArray();

        try (
                // Obtenemos conexión
                Connection conn = ConexionDB.getConexion();
                // Creamos un Statement simple porque no hay parámetros (?)
                Statement sfmt = conn.createStatement();
                // Ejecutamos la consulta y obtenemos los resultados
                ResultSet rs = sfmt.executeQuery(SQL_SELECT);) {

            // Verificamos si el ResultSet está vacío (si no tiene primer elemento)
            if (rs.next() == false) {
                // Creamos objeto para el mensaje de "Vacío"
                JsonObject datosVacios = new JsonObject();

                // Añadimos propiedad de mensaje
                datosVacios.addProperty("Mensaje", "No hay usuarios actualmente");
                // Añadimos código de estatus 404
                datosVacios.addProperty("status", 404);
                // Lo agregamos a la lista
                listarUsuarios.add(datosVacios);

            } else {
                // Si sí había datos, usamos do-while para no saltarnos el primero
                do {
                    // Creamos un objeto JSON nuevo para el usuario actual
                    JsonObject user = new JsonObject();

                    // Extraemos los datos de las columnas
                    String nombre = rs.getString("Nombre");
                    String correo = rs.getString("Correo");
                    int contraseña = rs.getInt("PasswordHash");

                    // Rellenamos el objeto JSON con los datos obtenidos
                    user.addProperty("Nombre", nombre);
                    user.addProperty("Correo", correo);
                    user.addProperty("Contraseña", contraseña);
                    // Agregamos status 200 (OK) a cada usuario
                    user.addProperty("status", 200);

                    // Añadimos este usuario a la lista final
                    listarUsuarios.add(user);
                } while (rs.next()); // Repetimos mientras queden filas

            }

        } catch (SQLException e) {
            // Manejo de errores SQL
            e.printStackTrace();
        }
        // Retornamos la lista completa
        return listarUsuarios;
    }

    /**
     * Verifica si un usuario existe en la base de datos dado su correo.
     * 
     * @param correo Correo del usuario a verificar.
     * @return true si el usuario existe, false en caso contrario.
     */
    public static boolean usuarioExistente(String correo) {

        // Variable bandera, por defecto asumimos que NO existe
        boolean correroExist = false;
        try (
                // Conectamos
                Connection conn = ConexionDB.getConexion();
                // Preparamos el SQL de búsqueda con filtro WHERE
                PreparedStatement sfmt = conn.prepareStatement(SQL_SELECT_WHERE);) {
            // Asignamos el correo que recibimos al parámetro (?)
            sfmt.setString(1, correo);
            // Ejecutamos la consulta
            ResultSet rs = sfmt.executeQuery();

            // Si rs.next() es true, significa que encontró al menos un registro
            if (rs.next()) {
                correroExist = true; // El usuario existe
            } else {
                correroExist = false; // No se encontró nada
            }

        } catch (SQLException e) {
            // Error en la consulta
            e.printStackTrace();
        }
        // Devolvemos el resultado booleano
        return correroExist;
    }

}
