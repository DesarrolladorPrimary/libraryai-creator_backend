package com.libraryai.backend.dao;

import java.sql.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.DbConnection;
import com.libraryai.backend.models.User;


//**Clase encargada de servir de puente entre el modelo y la DB
/**
 * DAO para operaciones de usuario.
 */
public class UserDao {
    // **Consultas SQL

    // Language=sql
    private final static String SQL_INSERT = """
            INSERT INTO Usuario(Nombre, Correo, PasswordHash, FechaRegistro, Activo) values (?,?,?,?,?)
            """;

    // language=sql
    private final static String SQL_SELECT_WHERE = """
            SELECT Correo FROM Usuario WHERE Correo = ?;
            """;

    // language=sql
    private final static String SQL_SELECT = """
            SELECT * FROM  V_RolesDeUsuario;
            """;

    // language=sql
    private final static String SQL_SELECT_WHEREID = """
            SELECT * FROM V_RolesDeUsuario WHERE PK_UsuarioID=?;
            """;

    // language=sql
    private final static String SQL_DELETE = """
            DELETE FROM Usuario WHERE `PK_UsuarioID` = ?;
            """;

    // language=sql
    private final static String SQL_UPDATE = """
            UPDATE Usuario SET Nombre = ?, Correo = ?, PasswordHash = ? WHERE  PK_UsuarioID=?;
            """;

    /**
     * Metodo encargado de crear la conexion, recibir los datos del modelo y
     * realizar la insercion para crear un usuario en la base de datos.
     *
     * @param usuario Objeto User con los datos a insertar.
     */
    public static int save(User usuario) {

        try (
                // Obtenemos la conexión a la base de datos desde la clase de configuración
                Connection conn = DbConnection.getConnection();
                // Preparamos la sentencia SQL usando la constante definida arriba
                PreparedStatement sfmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            
            ) {

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
            ResultSet rs = sfmt.getGeneratedKeys();
            int id = 0;

            // Si se afectó al menos una fila, significa que se guardó correctamente
            if (filasAfectadas > 0) {
                System.out.println("User añadido a la DB");

                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }
            
            return id;
        } catch (SQLException e) {
            // Si hay error SQL, lo imprimimos en la consola de errores
            System.err.println("Error al registrar el usuario: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Busca un usuario por id en la vista V_RolesDeUsuario.
     * Devuelve un JSON con campos de usuario y status (200/404).
     */
    public static JsonObject getById(int id) {
        JsonObject user = new JsonObject();
        try (
                Connection conn = DbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_WHEREID);) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();


            if (!rs.next()) {
                System.out.println("El usuario no existe");
                user.addProperty("status" , 404);
                user.addProperty("Mensaje", "No esxiste el usuario");

            } else {

                // Extraemos los datos de las columnas
                int usuario_id = rs.getInt("PK_UsuarioID");
                String nombre = rs.getString("Nombre");
                String correo = rs.getString("Correo");
                String rol = rs.getString("NombreRol");

                // Rellenamos el objeto JSON con los datos obtenidos
                user.addProperty("PK_UsuarioID", usuario_id);
                user.addProperty("Nombre", nombre);
                user.addProperty("Correo", correo);
                user.addProperty("Rol", rol);
                // Agregamos status 200 (OK) al usuario
                user.addProperty("status", 200);

                do {

                } while (rs.next());
            }

        } catch (SQLException e) {
            System.err.println();
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Recupera todos los usuarios de la base de datos.
     *
     * @return JsonArray con los usuarios encontrados o un mensaje de error si no
     * hay datos.
     */
    public static JsonArray listAllUsers() {

        // Creamos la lista JSON donde guardaremos los usuarios
        JsonArray listUsers = new JsonArray();

        try (
                // Obtenemos conexión
                Connection conn = DbConnection.getConnection();
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
                listUsers.add(datosVacios);

            } else {
                // Si sí había datos, usamos do-while para no saltarnos el primero
                do {
                    // Creamos un objeto JSON nuevo para el usuario actual
                    JsonObject user = new JsonObject();

                    // Extraemos los datos de las columnas
                    int usuario_id = rs.getInt("PK_UsuarioID");
                    String nombre = rs.getString("Nombre");
                    String correo = rs.getString("Correo");
                    String rol = rs.getString("NombreRol");

                    // Rellenamos el objeto JSON con los datos obtenidos
                    user.addProperty("PK_UsuarioID", usuario_id);
                    user.addProperty("Nombre", nombre);
                    user.addProperty("Correo", correo);
                    user.addProperty("Rol", rol);
                    // Agregamos status 200 (OK) a cada usuario
                    user.addProperty("status", 200);

                    // Añadimos este usuario a la lista final
                    listUsers.add(user);
                } while (rs.next()); // Repetimos mientras queden filas

            }

        } catch (SQLException e) {
            // Manejo de errores SQL
            e.printStackTrace();
        }
        // Retornamos la lista completa
        return listUsers;
    }

    /**
     * Verifica si un usuario existe en la base de datos dado su correo.
     *
     * @param correo Email del usuario a verificar.
     * @return true si el usuario existe, false en caso contrario.
     */
    public static boolean existsByEmail(String correo) {

        // Variable bandera, por defecto asumimos que NO existe
        boolean correroExist = false;
        try (
                // Conectamos
                Connection conn = DbConnection.getConnection();
                // Preparamos el SQL de búsqueda con filtro WHERE
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_WHERE);) {
            // Asignamos el correo que recibimos al parámetro (?)
            pstmt.setString(1, correo);
            // Ejecutamos la consulta
            ResultSet rs = pstmt.executeQuery();

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

    /**
     * Actualiza datos basicos del usuario.
     * Retorna status 200 si actualiza, 404 si no hubo cambios.
     */
    public static JsonObject updateUser(String nombre, String correo, String contraseña, int id){
        JsonObject json = new JsonObject();
        
        try (
            Connection conn = DbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE);
        ) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, correo);
            pstmt.setString(3, contraseña);
            pstmt.setInt(4, id);

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                json.addProperty("Mensaje", "El usuario fue actualizado correctamente");
                json.addProperty("status", 200);

            }
            else {
                json.addProperty("Mensaje", "No se realizaron cambios");
                json.addProperty("status", 404);

            }
        

        } catch (SQLException e) {
            
            e.printStackTrace();
            json.addProperty("status", 500);
        }
        return json;
    }


    /**
     * Elimina un usuario por id y devuelve resultado con status.
     */
    public static JsonObject deleteById(int id) {

        JsonObject response = new JsonObject();

        try (
                Connection conn = DbConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE);) {

            pstmt.setInt(1, id);
            int filasAfectadas = pstmt.executeUpdate();
            boolean exist = false;

            if (filasAfectadas > 0) {
                System.out.println("User eliminado");
                exist = true;
                response.addProperty("Mensaje", "User eliminado correctamente");
                response.addProperty("status", 200);

            } else {
                System.out.println("El usuario no existe con ese id");
                exist = false;
                response.addProperty("Mensaje", "El usuario no existe con ese id");
                response.addProperty("status", 404);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

}
