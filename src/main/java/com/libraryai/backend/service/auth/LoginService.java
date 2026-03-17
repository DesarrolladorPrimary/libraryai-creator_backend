package com.libraryai.backend.service.auth;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.auth.LoginDao;
import com.libraryai.backend.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Servicio de negocio para autenticación de usuarios.
 *
 * <p>Valida formato de entrada, estado del correo, compatibilidad del hash y
 * finalmente emite el JWT que usa el resto del backend.
 */
public class LoginService {

    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,100}";

    /**
     * Valida formato de correo, verifica credenciales y genera token JWT.
     */
    public static JsonObject validateLoginData(String correo, String contraseña) {
        
        JsonObject response = new JsonObject();
        try {


            if (!correo.matches(EMAIL_PATTERN)) {
                response.addProperty("Mensaje", "El correo no es válido");
                response.addProperty("status", 400);

            } else {
                // Busca usuario por correo en la DB.
                JsonObject user = LoginDao.findUserByEmail(correo);

                int code = user.get("status").getAsInt();

                // Si el usuario no existe o hay error, devolvemos la respuesta.
                if (code != 200) {
                    response = user;
                    return response;
                }

                String contraseñaDB = user.get("contraseña").getAsString();
                String rol = user.get("rol").getAsString();
                int id = user.get("id").getAsInt();
                boolean correoVerificado = user.get("correoVerificado").getAsBoolean();

                // El login reusa el mismo flujo de verificación para reenviar enlaces
                // cuando el usuario todavía no confirmó su correo.
                if (!correoVerificado) {
                    // Generar nuevo token de verificación al momento del login
                    JsonObject resultado = com.libraryai.backend.service.auth.RecuperacionService.generarTokenVerificacionLogin(correo, id);
                    
                    if (resultado.get("status").getAsInt() == 200) {
                        response.addProperty("Mensaje", "Debe verificar su correo. Te enviamos un nuevo enlace de verificación.");
                        response.addProperty("correoEnviado", true);
                    } else {
                        response.addProperty("Mensaje", "Debe verificar su correo. No pudimos reenviar el enlace en este momento.");
                        response.addProperty("correoEnviado", false);
                    }
                    response.addProperty("status", 403);
                    return response;
                }

                if (!matchesPassword(contraseña, contraseñaDB)) {
                    response.addProperty("Mensaje", "La contraseña no coincide con la registrada");
                    response.addProperty("status", 400);
                    return response;
                }

                // El token resume identidad, rol e id para autorización posterior.
                String token = JwtUtil.generateUserToken(correo, rol, id);
                response.addProperty("Mensaje", "Inicio de sesión correcto");
                response.addProperty("Token", token);
                response.addProperty("status", 200);

            }

            return response;

        } catch (Throwable e) {
            e.printStackTrace();

            response.addProperty("Mensaje" , "Servidor ha fallado");
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Compara la contraseña plana contra el hash persistido tolerando prefijos
     * bcrypt legados que la librería actual no interpreta directamente.
     */
    private static boolean matchesPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, normalizeBcryptRevision(storedHash));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * Normaliza revisiones bcrypt antiguas para mantener compatibilidad con
     * usuarios sembrados o migrados desde otros entornos.
     */
    private static String normalizeBcryptRevision(String hash) {
        if (hash.startsWith("$2y$") || hash.startsWith("$2x$")) {
            return "$2a$" + hash.substring(4);
        }

        return hash;
    }
}
 
