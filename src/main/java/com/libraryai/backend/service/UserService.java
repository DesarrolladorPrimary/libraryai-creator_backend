package com.libraryai.backend.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.UserRoleDao;
import com.libraryai.backend.dao.UserDao;
import com.libraryai.backend.dao.SettingsDao;
import com.libraryai.backend.dao.auth.RecuperacionDao;
import com.libraryai.backend.models.User;

/**
 * Servicio de negocio para usuarios.
 */
public class UserService {

    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,100}";
    private static final String NAME_PATTERN = "^[\\p{L}][\\p{L}' -]*$";
    private static final String PASSWORD_COMPLEXITY_PATTERN = ".*[\\d\\W_].*";
    private static final int EMAIL_MAX_LENGTH = 120;
    private static final int NAME_MIN_LENGTH = 3;
    private static final int NAME_MAX_LENGTH = 25;

    /**
     * Valida la política de contraseñas usada en registro, actualización y
     * recuperación.
     *
     * @param contraseña Contraseña sin hashear.
     * @return JsonObject con error o {@code null} si la contraseña es válida.
     */
    public static JsonObject validatePasswordPolicy(String contraseña) {
        JsonObject response = new JsonObject();

        if (contraseña == null || contraseña.isBlank()) {
            response.addProperty("Mensaje", "La contraseña es obligatoria");
            response.addProperty("status", 400);
            return response;
        }

        if (contraseña.length() < 8) {
            response.addProperty("Mensaje", "La contraseña debe tener mínimo 8 caracteres");
            response.addProperty("status", 400);
            return response;
        }

        if (!contraseña.matches(PASSWORD_COMPLEXITY_PATTERN)) {
            response.addProperty("Mensaje", "La contraseña debe incluir un número o símbolo");
            response.addProperty("status", 400);
            return response;
        }

        return null;
    }

    /**
     * Valida la política de nombres usada en registro y actualización.
     *
     * @param nombre Nombre sin normalizar.
     * @return JsonObject con error o {@code null} si el nombre es válido.
     */
    public static JsonObject validateNamePolicy(String nombre) {
        JsonObject response = new JsonObject();
        String normalizedName = nombre == null ? "" : nombre.trim();

        if (normalizedName.isBlank()) {
            response.addProperty("Mensaje", "El nombre es obligatorio");
            response.addProperty("status", 400);
            return response;
        }

        if (normalizedName.length() < NAME_MIN_LENGTH) {
            response.addProperty("Mensaje", "El nombre debe tener al menos 3 caracteres");
            response.addProperty("status", 400);
            return response;
        }

        if (normalizedName.length() > NAME_MAX_LENGTH) {
            response.addProperty("Mensaje", "El nombre no puede superar los 25 caracteres");
            response.addProperty("status", 400);
            return response;
        }

        if (!normalizedName.matches(NAME_PATTERN)) {
            response.addProperty("Mensaje", "El nombre solo puede contener letras, espacios, apóstrofes o guiones");
            response.addProperty("status", 400);
            return response;
        }

        return null;
    }

    /**
     * Valida la política de correos usada en registro y actualización.
     *
     * @param correo Correo sin normalizar.
     * @return JsonObject con error o {@code null} si el correo es válido.
     */
    public static JsonObject validateEmailPolicy(String correo) {
        JsonObject response = new JsonObject();
        String normalizedEmail = correo == null ? "" : correo.trim();

        if (normalizedEmail.isBlank()) {
            response.addProperty("Mensaje", "El correo es obligatorio");
            response.addProperty("status", 400);
            return response;
        }

        if (normalizedEmail.length() > EMAIL_MAX_LENGTH) {
            response.addProperty("Mensaje", "El correo no puede superar los 120 caracteres");
            response.addProperty("status", 400);
            return response;
        }

        if (!normalizedEmail.matches(EMAIL_PATTERN)) {
            response.addProperty("Mensaje", "Correo no válido");
            response.addProperty("status", 400);
            return response;
        }

        return null;
    }

    /**
     * Valida los datos del usuario y coordina la creación si todo es correcto.
     *
     * @param nombre     Nombre del usuario.
     * @param correo     Correo electrónico.
     * @param contraseña Contraseña (numérica en este ejemplo).
     * @return JsonObject con el resultado de la operación y el código de estado.
     */
    public static JsonObject validateUserData(String nombre, String correo, String contraseña) {

        // Objeto JSON para devolver la respuesta
        JsonObject rJsonObject = new JsonObject();

        // Validamos que los datos básicos no sean null y la contraseña sea positiva
        if (nombre != null && correo != null && contraseña != null && !contraseña.isBlank()) {

            JsonObject nameValidation = validateNamePolicy(nombre);
            if (nameValidation == null) {
                nombre = nombre.trim();
                JsonObject emailValidation = validateEmailPolicy(correo);
                if (emailValidation != null) {
                    return emailValidation;
                }
                correo = correo.trim();

                if (UserDao.existsByEmail(correo)) {
                    // Error: El usuario ya existe
                    rJsonObject.addProperty("Mensaje", "Usuario ya existente con ese correo");
                    rJsonObject.addProperty("status", 409); // Conflict
                    return rJsonObject;
                }

                JsonObject passwordValidation = validatePasswordPolicy(contraseña);
                if (passwordValidation != null) {
                    return passwordValidation;
                }

                // Hasheamos la contraseña
                String contraseñaHash = BCrypt.hashpw(contraseña, BCrypt.gensalt());

                // Creamos el objeto Modelo 'Usuario' con todos los datos
                User user = new User(0, nombre, correo, contraseñaHash, LocalDate.now(), true);

                // Le decimos al DAO que lo guarde en la BD
                int id = UserDao.save(user);

                if (id != 0) {
                    if (!UserRoleDao.assignRole(id)) {
                        UserDao.deleteById(id);
                        rJsonObject.addProperty("Mensaje", "No fue posible asignar el rol inicial del usuario");
                        rJsonObject.addProperty("status", 500);
                        return rJsonObject;
                    }

                    JsonObject subscriptionAssignment = SettingsDao.assignDefaultFreeSubscription(id);
                    if (!subscriptionAssignment.has("status") || subscriptionAssignment.get("status").getAsInt() != 200) {
                        UserDao.deleteById(id);
                        rJsonObject.addProperty("Mensaje", "No fue posible asignar el plan gratuito inicial");
                        rJsonObject.addProperty("status", 500);
                        return rJsonObject;
                    }

                    // Generar token de verificación de correo
                    String tokenVerificacion = UUID.randomUUID().toString();
                    LocalDateTime expiracion = LocalDateTime.now().plusHours(24);
                    RecuperacionDao.guardarToken(id, tokenVerificacion, expiracion, "Verificacion_Registro");

                    // Enviar correo de verificación
                    EmailService.enviarCorreoVerificacion(id, correo, tokenVerificacion);

                    // Preparamos respuesta de éxito
                    rJsonObject.addProperty("Mensaje", "Usuario registrado correctamente. Revisa tu correo real y verifica la cuenta.");
                    rJsonObject.addProperty("status", 201); // Created
                } else {
                    rJsonObject.addProperty("Mensaje", "Error del servidor");
                    rJsonObject.addProperty("status", 500); // Error
                }
            } else {
                return nameValidation;
            }

        } else {
            // Error: Algún dato venía nulo o incorrecto
            rJsonObject.addProperty("Mensaje", "Datos vacíos");
            rJsonObject.addProperty("status", 400); // Bad Request

        }

        // Retornamos el JSON con el resultado
        return rJsonObject;
    }

    /**
     * Valida datos de actualización y delega al DAO.
     * Completa campos vacíos con valores actuales de la DB.
     */
    public static JsonObject validateUpdateData(String nombre, String correo, String contraseña, int id)
            throws IOException {

        // Obtiene el usuario actual para completar valores faltantes.
        JsonObject datos = UserDao.findById(id);

        JsonObject response = new JsonObject();

        int estado = datos.get("status").getAsInt();

        if (estado == 404) {
            response.addProperty("status", 404);
            response.addProperty("Mensaje", "Usuario no encontrado");
            return response;

        } else {

            // Valores actuales en DB.
            String nombreDB = datos.get("Nombre").getAsString();
            String correoDB = datos.get("Correo").getAsString();
            String contraseñaDB = UserDao.findPasswordHashById(id);
            if (contraseñaDB == null || contraseñaDB.isBlank()) {
                response.addProperty("Mensaje", "No fue posible recuperar la contraseña actual del usuario");
                response.addProperty("status", 500);
                return response;
            }

            // Si el cliente no envia un campo, se conserva el actual.
            if (nombre.isEmpty()) {
                nombre = nombreDB;
            } else {
                JsonObject nameValidation = validateNamePolicy(nombre);
                if (nameValidation != null) {
                    return nameValidation;
                }
                nombre = nombre.trim();
            }

            if (correo.isEmpty()) {
                correo = correoDB;
            } else {
                JsonObject emailValidation = validateEmailPolicy(correo);
                if (emailValidation != null) {
                    return emailValidation;
                }
                correo = correo.trim();
            }

            if (contraseña.isBlank()) {
                contraseña = contraseñaDB;

            } else {
                JsonObject passwordValidation = validatePasswordPolicy(contraseña);
                if (passwordValidation != null) {
                    return passwordValidation;
                }
                contraseña = BCrypt.hashpw(contraseña, BCrypt.gensalt());
            }

            response = UserDao.updateUser(nombre, correo, contraseña, id);
        }

        return response;
    }

    /**
     * Actualiza un solo campo del usuario.
     * 
     * @param campo Campo a actualizar (nombre, correo, contraseña).
     * @param valor Nuevo valor.
     * @param id ID del usuario.
     * @return JsonObject con el resultado.
     */
    public static JsonObject updateCampo(String campo, String valor, int id) {
        JsonObject response = new JsonObject();
        
        // Validaciones según el campo
        if (campo.equalsIgnoreCase("correo")) {
            JsonObject emailValidation = validateEmailPolicy(valor);
            if (emailValidation != null) {
                return emailValidation;
            }
            valor = valor.trim();
        }

        if (campo.equalsIgnoreCase("nombre")) {
            JsonObject nameValidation = validateNamePolicy(valor);
            if (nameValidation != null) {
                return nameValidation;
            }
            valor = valor.trim();
        }

        if (campo.equalsIgnoreCase("contraseña")) {
            JsonObject passwordValidation = validatePasswordPolicy(valor);
            if (passwordValidation != null) {
                return passwordValidation;
            }

            valor = BCrypt.hashpw(valor, BCrypt.gensalt());
        }

        return UserDao.updateCampo(campo, valor, id);
    }

}
