package com.libraryai.backend.dao.user;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;
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
    private final static String SQL_SELECT_PASSWORD_BY_ID = """
            SELECT PasswordHash FROM Usuario WHERE PK_UsuarioID = ?;
            """;

    // language=sql
    private final static String SQL_DELETE = """
            DELETE FROM Usuario WHERE `PK_UsuarioID` = ?;
            """;

    // language=sql
    private final static String SQL_SELECT_ID = """
            SELECT PK_UsuarioID FROM Usuario WHERE PK_UsuarioID = ?;
            """;

    // language=sql
    private final static String SQL_SELECT_FILE_PATHS_BY_USER = """
            SELECT RutaAlmacenamiento FROM ArchivoUsuario WHERE FK_UsuarioID = ?;
            """;

    // language=sql
    private final static String SQL_UPDATE = """
            UPDATE Usuario SET Nombre = ?, Correo = ?, PasswordHash = ? WHERE  PK_UsuarioID=?;
            """;

    // language=sql
    private static final String SQL_DELETE_EMAILS_BY_USER = """
            DELETE FROM Correo WHERE FK_UsuarioID = ?;
            """;

    // language=sql
    private static final String SQL_DELETE_TOKENS_BY_USER = """
            DELETE FROM TokenAcceso WHERE FK_UsuarioID = ?;
            """;

    // language=sql
    private static final String SQL_DELETE_SUBSCRIPTIONS_BY_USER = """
            DELETE FROM Suscripcion WHERE FK_UsuarioID = ?;
            """;

    // language=sql
    private static final String SQL_DELETE_FILES_BY_USER = """
            DELETE FROM ArchivoUsuario WHERE FK_UsuarioID = ?;
            """;

    // language=sql
    private static final String SQL_DELETE_USER_ROLES_BY_USER = """
            DELETE FROM UsuarioRol WHERE FK_UsuarioID = ?;
            """;

    // language=sql
    private static final String SQL_DELETE_PAYMENTS_BY_USER = """
            DELETE FROM Pago
            WHERE FK_SuscripcionID IN (
                SELECT PK_SuscripcionID
                FROM (
                    SELECT PK_SuscripcionID
                    FROM Suscripcion
                    WHERE FK_UsuarioID = ?
                ) suscripciones_usuario
            );
            """;

    // language=sql
    private static final String SQL_DELETE_STORIES_BY_USER = """
            DELETE FROM Relato WHERE FK_UsuarioID = ?;
            """;

    // language=sql
    private static final String SQL_DELETE_STORY_VERSIONS_BY_USER = """
            DELETE FROM RelatoVersion
            WHERE FK_RelatoID IN (
                SELECT PK_RelatoID
                FROM (
                    SELECT PK_RelatoID
                    FROM Relato
                    WHERE FK_UsuarioID = ?
                ) relatos_usuario
            );
            """;

    // language=sql
    private static final String SQL_DELETE_AI_CONFIG_BY_USER = """
            DELETE FROM ConfiguracionIA
            WHERE FK_RelatoID IN (
                SELECT PK_RelatoID
                FROM (
                    SELECT PK_RelatoID
                    FROM Relato
                    WHERE FK_UsuarioID = ?
                ) relatos_usuario
            );
            """;

    // language=sql
    private static final String SQL_DELETE_CHAT_MESSAGES_BY_USER = """
            DELETE FROM MensajeChat
            WHERE FK_RelatoID IN (
                SELECT PK_RelatoID
                FROM (
                    SELECT PK_RelatoID
                    FROM Relato
                    WHERE FK_UsuarioID = ?
                ) relatos_usuario
            );
            """;

    // language=sql
    private static final String SQL_DELETE_STORY_FILE_LINKS_BY_USER = """
            DELETE FROM Relato_ArchivoFuente
            WHERE FK_RelatoID IN (
                SELECT PK_RelatoID
                FROM (
                    SELECT PK_RelatoID
                    FROM Relato
                    WHERE FK_UsuarioID = ?
                ) relatos_usuario
            )
               OR FK_ArchivoID IN (
                SELECT PK_ArchivoID
                FROM (
                    SELECT PK_ArchivoID
                    FROM ArchivoUsuario
                    WHERE FK_UsuarioID = ?
                ) archivos_usuario
            );
            """;

    // language=sql
    private static final String SQL_DELETE_ROLE_AUDIT_BY_USER = """
            DELETE FROM AuditoriaRolUsuario
            WHERE FK_UsuarioAfectadoID = ? OR FK_AdminID = ?;
            """;

    // language=sql
    private static final String SQL_DELETE_MODERATION_LOGS_BY_USER = """
            DELETE FROM LogModeracion WHERE FK_UsuarioID = ?;
            """;

    /**
     * Metodo encargado de crear la conexion, recibir los datos del modelo y
     * realizar la insercion para crear un usuario en la base de datos.
     *
     * @param usuario Objeto Usuario con los datos a insertar.
     */
    public static int save(User usuario) {

        try (
                // Obtenemos la conexión a la base de datos desde la clase de configuración
                Connection conn = DatabaseConnection.getConnection();
                // Preparamos la sentencia SQL usando la constante definida arriba
                PreparedStatement sfmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            
            ) {

            // Asignamos el nombre al primer parámetro (?) del SQL
            sfmt.setString(1, usuario.getName());
            // Asignamos el correo al segundo parámetro
            sfmt.setString(2, usuario.getEmail());
            // Asignamos el hash de la contraseña al tercer parámetro
            sfmt.setString(3, usuario.getPasswordHash());
            // Convertimos la fecha LocalDate a Date de SQL y la asignamos
            sfmt.setDate(4, Date.valueOf(usuario.getRegistrationDate()));
            // Asignamos el booleano 'Activo'
            sfmt.setBoolean(5, usuario.isActive());

            // Ejecutamos la actualización en la DB y guardamos cuántas filas se afectaron
            int filasAfectadas = sfmt.executeUpdate();
            ResultSet rs = sfmt.getGeneratedKeys();
            int id = 0;

            // Si se afectó al menos una fila, significa que se guardó correctamente
            if (filasAfectadas > 0) {
                System.out.println("Usuario añadido a la DB");

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
    public static JsonObject findById(int id) {
        JsonObject user = new JsonObject();
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_WHEREID);) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();


            if (!rs.next()) {
                System.out.println("El usuario no existe");
                user.addProperty("status" , 404);
                user.addProperty("Mensaje", "No existe el usuario");

            } else {

                // Extraemos los datos de las columnas
                int usuario_id = rs.getInt("PK_UsuarioID");
                String nombre = rs.getString("Nombre");
                String correo = rs.getString("Correo");
                String rol = rs.getString("NombreRol");
                Date fechaRegistro = rs.getDate("FechaRegistro");
                String fotoPerfil = rs.getString("FotoPerfil");

                // Rellenamos el objeto JSON con los datos obtenidos
                user.addProperty("PK_UsuarioID", usuario_id);
                user.addProperty("Nombre", nombre);
                user.addProperty("Correo", correo);
                user.addProperty("Rol", rol);
                user.addProperty("Fecha Registro", fechaRegistro.toString());
                user.addProperty("FotoPerfil", fotoPerfil != null ? fotoPerfil : "");
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
     * Obtiene el hash de contraseña actual de un usuario.
     *
     * @param id ID del usuario.
     * @return Hash almacenado o {@code null} si no existe/no pudo consultarse.
     */
    public static String findPasswordHashById(int id) {
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_PASSWORD_BY_ID)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("PasswordHash");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Recupera todos los usuarios de la base de datos.
     *
     * @return JsonArray con los usuarios encontrados o un mensaje de error si no
     * hay datos.
     */
    public static JsonArray findAll() {

        // Creamos la lista JSON donde guardaremos los usuarios
        JsonArray listUsers = new JsonArray();

        try (
                // Obtenemos conexión
                Connection conn = DatabaseConnection.getConnection();
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
     * @param correo Correo del usuario a verificar.
     * @return true si el usuario existe, false en caso contrario.
     */
    public static boolean existsByEmail(String correo) {

        // Variable bandera, por defecto asumimos que NO existe
        boolean correoExist = false;
        try (
                // Conectamos
                Connection conn = DatabaseConnection.getConnection();
                // Preparamos el SQL de búsqueda con filtro WHERE
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_WHERE);) {
            // Asignamos el correo que recibimos al parámetro (?)
            pstmt.setString(1, correo);
            // Ejecutamos la consulta
            ResultSet rs = pstmt.executeQuery();

            // Si rs.next() es true, significa que encontró al menos un registro
            if (rs.next()) {
                correoExist = true; // El usuario existe
            } else {
                correoExist = false; // No se encontró nada
            }

        } catch (SQLException e) {
            // Error en la consulta
            e.printStackTrace();
        }
        // Devolvemos el resultado booleano
        return correoExist;
    }

    /**
     * Actualiza datos basicos del usuario.
     * Retorna status 200 si actualiza, 404 si no hubo cambios.
     */
    public static JsonObject updateUser(String nombre, String correo, String contraseña, int id){
        JsonObject json = new JsonObject();
        
        try (
            Connection conn = DatabaseConnection.getConnection();
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
     * Actualiza un campo específico del usuario.
     * 
     * @param campo Nombre del campo a actualizar (nombre, correo, password).
     * @param valor Nuevo valor para el campo.
     * @param id ID del usuario.
     * @return JsonObject con el resultado.
     */
    public static JsonObject updateCampo(String campo, String valor, int id) {
        JsonObject json = new JsonObject();
        
        String sql = "";
        
        switch (campo.toLowerCase()) {
            case "nombre":
                sql = "UPDATE Usuario SET Nombre = ? WHERE PK_UsuarioID = ?";
                break;
            case "correo":
                sql = "UPDATE Usuario SET Correo = ? WHERE PK_UsuarioID = ?";
                break;
            case "password":
            case "contraseña":
                sql = "UPDATE Usuario SET PasswordHash = ? WHERE PK_UsuarioID = ?";
                break;
            default:
                json.addProperty("Mensaje", "Campo no válido. Usa: nombre, correo o contraseña");
                json.addProperty("status", 400);
                return json;
        }
        
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, valor);
            pstmt.setInt(2, id);
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                json.addProperty("Mensaje", "Campo actualizado correctamente");
                json.addProperty("status", 200);
            } else {
                json.addProperty("Mensaje", "Usuario no encontrado");
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
        List<String> filePaths = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (!userExists(conn, id)) {
                    response.addProperty("Mensaje", "El usuario no existe con ese id");
                    response.addProperty("status", 404);
                    return response;
                }

                filePaths = listUserFilePaths(conn, id);

                executeDelete(conn, SQL_DELETE_MODERATION_LOGS_BY_USER, id);
                executeDelete(conn, SQL_DELETE_ROLE_AUDIT_BY_USER, id, id);
                executeDelete(conn, SQL_DELETE_EMAILS_BY_USER, id);
                executeDelete(conn, SQL_DELETE_TOKENS_BY_USER, id);
                executeDelete(conn, SQL_DELETE_PAYMENTS_BY_USER, id);
                executeDelete(conn, SQL_DELETE_SUBSCRIPTIONS_BY_USER, id);
                executeDelete(conn, SQL_DELETE_USER_ROLES_BY_USER, id);
                executeDelete(conn, SQL_DELETE_STORY_VERSIONS_BY_USER, id);
                executeDelete(conn, SQL_DELETE_AI_CONFIG_BY_USER, id);
                executeDelete(conn, SQL_DELETE_CHAT_MESSAGES_BY_USER, id);
                executeDelete(conn, SQL_DELETE_STORY_FILE_LINKS_BY_USER, id, id);
                executeDelete(conn, SQL_DELETE_STORIES_BY_USER, id);
                executeDelete(conn, SQL_DELETE_FILES_BY_USER, id);

                int filasAfectadas = executeDelete(conn, SQL_DELETE, id);
                if (filasAfectadas <= 0) {
                    conn.rollback();
                    response.addProperty("Mensaje", "El usuario no existe con ese id");
                    response.addProperty("status", 404);
                    return response;
                }

                conn.commit();
                appendFileCleanupWarning(cleanupPhysicalFiles(filePaths), response);
                response.addProperty("Mensaje", "Usuario eliminado correctamente");
                response.addProperty("status", 200);
                System.out.println("Usuario eliminado");
            } catch (SQLException e) {
                conn.rollback();
                response.addProperty("Mensaje", "No fue posible eliminar el usuario: " + e.getMessage());
                response.addProperty("status", 500);
            }
        } catch (SQLException e) {
            response.addProperty("Mensaje", "No fue posible preparar la eliminación del usuario: " + e.getMessage());
            response.addProperty("status", 500);
        }

        return response;
    }

    private static boolean userExists(Connection conn, int id) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_ID)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    private static List<String> listUserFilePaths(Connection conn, int userId) throws SQLException {
        List<String> filePaths = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_FILE_PATHS_BY_USER)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String path = rs.getString("RutaAlmacenamiento");
                if (path != null && !path.isBlank()) {
                    filePaths.add(path);
                }
            }
        }

        return filePaths;
    }

    private static int executeDelete(Connection conn, String sql, int... params) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int index = 0; index < params.length; index++) {
                pstmt.setInt(index + 1, params[index]);
            }
            return pstmt.executeUpdate();
        }
    }

    private static int cleanupPhysicalFiles(List<String> filePaths) {
        int failedDeletions = 0;

        for (String filePath : filePaths) {
            try {
                Files.deleteIfExists(Path.of(filePath));
            } catch (Exception e) {
                failedDeletions++;
            }
        }

        return failedDeletions;
    }

    private static void appendFileCleanupWarning(int failedDeletions, JsonObject response) {
        if (failedDeletions <= 0) {
            return;
        }

        response.addProperty("detalleArchivos",
                "Se eliminaron los registros del usuario, pero " + failedDeletions
                        + " archivo(s) no pudieron borrarse del disco");
    }

    /**
     * Actualiza la foto de perfil del usuario.
     * 
     * @param fotoPerfil Ruta o URL de la foto de perfil.
     * @param id ID del usuario.
     * @return JsonObject con el resultado.
     */
    public static JsonObject updateFotoPerfil(String fotoPerfil, int id) {
        JsonObject json = new JsonObject();
        
        String sql = "UPDATE Usuario SET FotoPerfil = ? WHERE PK_UsuarioID = ?";
        
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, fotoPerfil);
            pstmt.setInt(2, id);
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                json.addProperty("Mensaje", "Foto de perfil actualizada");
                json.addProperty("status", 200);
            } else {
                json.addProperty("Mensaje", "Usuario no encontrado");
                json.addProperty("status", 404);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            json.addProperty("status", 500);
        }
        
        return json;
    }

    /**
     * Marca el correo del usuario como verificado.
     * 
     * @param usuarioId ID del usuario.
     * @return true si se actualizó correctamente.
     */
    public static boolean verificarCorreo(int usuarioId) {
        String sql = "UPDATE Usuario SET CorreoVerificado = TRUE, FechaVerificacion = NOW() WHERE PK_UsuarioID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, usuarioId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
