package com.libraryai.backend.service;

import java.io.IOException;
import java.time.LocalDate;

import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.UserRoleDao;
import com.libraryai.backend.dao.UserDao;
import com.libraryai.backend.models.User;

/**
 * Servicio de negocio para usuarios.
 */
public class UserService {

    /**
     * Valida los datos del usuario y coordina la creación si todo es correcto.
     *
     * @param nombre     Nombre del usuario.
     * @param correo     Email electrónico.
     * @param contraseña Contraseña (numérica en este ejemplo).
     * @return JsonObject con el resultado de la operación y el código de estado.
     */
    public static JsonObject validateUserData(String nombre, String correo, String contraseña) {

        // Objeto JSON para devolver la respuesta
        JsonObject rJsonObject = new JsonObject();
        // Llamamos al DAO para ver si el correo ya existe en DB
        boolean correoDB = UserDao.existsByEmail(correo);

        // Validamos que los datos básicos no sean null y la contraseña sea positiva
        if (!(nombre == null) && !(correo == null) && !(contraseña.isBlank())) {

            if (nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+") && nombre.length() > 2 && !(nombre.matches("@-$"))) {
                // Si el correo NO existe en la base de datos...
                if (correoDB == false) { 

                    // Validación simple de formato de correo (debe tener @)
                    if (correo.matches("[a-zA-Z]+\\d*@[a-zA-Z]+\\.[a-zA-Z]{2,6}")) {


                        // Hasheamos la contraseña
                        String contraseñaHash = BCrypt.hashpw(contraseña, BCrypt.gensalt());

                        // Creamos el objeto Modelo 'User' con todos los datos
                        User user = new User(0, nombre, correo, contraseñaHash, LocalDate.now(), true);

                        // Le decimos al DAO que lo guarde en la BD
                        int id = UserDao.save(user);

                        if (id != 0) {
                            UserRoleDao.assignRole(id);

                            // Preparamos respuesta de éxito
                            rJsonObject.addProperty("Mensaje", "User creado correctamente");
                            rJsonObject.addProperty("status", 201); // Created
                        } else {
                            rJsonObject.addProperty("Mensaje", "Error del servidor");
                            rJsonObject.addProperty("status", 500); // Error
                        }

                    } else {
                        // Error: correo sin @
                        rJsonObject.addProperty("Mensaje", "Email no valido");
                        rJsonObject.addProperty("status", 400); // Bad Request

                    }
                } else {
                    // Error: El usuario ya existe
                    rJsonObject.addProperty("Mensaje", "User ya existente con ese correo");
                    rJsonObject.addProperty("status", 409); // Conflict
                }
            } else {
                // Error: Algún dato venía nulo o incorrecto
                rJsonObject.addProperty("Mensaje", "Nombre de usuario incorrecto");
                rJsonObject.addProperty("status", 400); // Bad Request
            }

        } else {
            // Error: Algún dato venía nulo o incorrecto
            rJsonObject.addProperty("Mensaje", "Datos vacios");
            rJsonObject.addProperty("status", 400); // Bad Request

        }

        // Retornamos el JSON con el resultado
        return rJsonObject;
    }

    /**
     * Valida datos de actualizacion y delega al DAO.
     * Completa campos vacios con valores actuales de la DB.
     */
    public static JsonObject validateUserUpdate(String nombre, String correo, String contraseña, int id)
            throws IOException {

        // Obtiene el usuario actual para completar valores faltantes.
        JsonObject datos = UserDao.getById(id);

        JsonObject response = new JsonObject();

        int estado = datos.get("status").getAsInt();

        if (estado == 404) {
            response.addProperty("status", 404);
            response.addProperty("Mensaje", "User no encontrado");
            return response;

        } else {

            // Valores actuales en DB.
            String nombreDB = datos.get("Nombre").getAsString();
            String correoDB = datos.get("Correo").getAsString();
            String contraseñaDB = datos.get("Contraseña").getAsString();

            // Si el cliente no envia un campo, se conserva el actual.
            if (nombre.isEmpty()) {
                nombre = nombreDB;
            }

            if (correo.isEmpty()) {
                correo = correoDB;
            }

            if (contraseña.isBlank()) {
                contraseña = contraseñaDB;

            } else {
                contraseña = BCrypt.hashpw(contraseña, BCrypt.gensalt());
            }

            if (correo.matches("[a-zA-Z]+@[a-zA-Z]+\\.[a-zA-Z]{2,6}")) {

                response = UserDao.updateUser(nombre, correo, contraseña, id);
            }

            else {
                response.addProperty("Mensaje", "Email no valido");
                response.addProperty("status", 400);
            }
        }

        return response;
    }

}
