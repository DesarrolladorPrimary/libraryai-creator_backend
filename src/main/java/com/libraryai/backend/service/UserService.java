package com.libraryai.backend.service;

import java.time.LocalDate;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.UsuariosDao;
import com.libraryai.backend.models.Usuario;

public class UserService {

    /**
     * Valida los datos del usuario y coordina la creación si todo es correcto.
     * 
     * @param nombre     Nombre del usuario.
     * @param correo     Correo electrónico.
     * @param contraseña Contraseña (numérica en este ejemplo).
     * @return JsonObject con el resultado de la operación y el código de estado.
     */
    public static JsonObject verificarDatosUsuario(String nombre, String correo, int contraseña) {

        // Objeto JSON para devolver la respuesta
        JsonObject rJsonObject = new JsonObject();
        // Llamamos al DAO para ver si el correo ya existe en DB
        boolean correoDB = UsuariosDao.usuarioExistente(correo);

        // Validamos que los datos básicos no sean null y la contraseña sea positiva
        if (!(nombre == null) && !(correo == null) && !(contraseña < 0)) {
            // Si el correo NO existe en la base de datos...
            if (correoDB == false) {

                // Validación simple de formato de correo (debe tener @)
                if (correo.contains("@")) {

                    // Convertimos la contraseña int a String (para guardarla, idealmente se
                    // hashearia)
                    String contraseñaHash = Integer.toString(contraseña);

                    // Creamos el objeto Modelo 'Usuario' con todos los datos
                    Usuario user = new Usuario(0, nombre, correo, contraseñaHash, LocalDate.now(), true);

                    // Le decimos al DAO que lo guarde en la BD
                    UsuariosDao.registrarUsuario(user);

                    // Preparamos respuesta de éxito
                    rJsonObject.addProperty("Mensaje", "Usuario creado correctamente");
                    rJsonObject.addProperty("status", 201); // Created
                } else {
                    // Error: correo sin @
                    rJsonObject.addProperty("Mensaje", "Correo no valido");
                    rJsonObject.addProperty("status", 400); // Bad Request

                }
            } else {
                // Error: El usuario ya existe
                rJsonObject.addProperty("Mensaje", "Usuario ya existente con ese correo");
                rJsonObject.addProperty("status", 409); // Conflict
            }
        } else {
            // Error: Algún dato venía nulo o incorrecto
            rJsonObject.addProperty("Mensaje", "Datos vacios");
            rJsonObject.addProperty("status", 400); // Bad Request

        }

        // Retornamos el JSON con el resultado
        return rJsonObject;
    }
}
