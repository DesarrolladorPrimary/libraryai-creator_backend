package com.libraryai.backend.service.auth;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.auth.LoginDao;
import com.libraryai.backend.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Servicio de negocio para autenticacion de usuarios.
 *
 * <p>Valida formato de entrada, estado de la cuenta, correo verificado,
 * compatibilidad del hash y finalmente emite el JWT que usa el resto del
 * backend.
 */
public class LoginService {

    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,100}";

    /**
     * Valida formato de correo, verifica credenciales y genera token JWT.
     */
    public static JsonObject validateLoginData(String correo, String contrasena) {

        JsonObject response = new JsonObject();
        try {

            if (!correo.matches(EMAIL_PATTERN)) {
                response.addProperty("Mensaje", "El correo no es valido");
                response.addProperty("status", 400);

            } else {
                JsonObject user = LoginDao.findUserByEmail(correo);

                int code = user.get("status").getAsInt();

                if (code != 200) {
                    response = user;
                    return response;
                }

                String passwordDb = user.get("contrasena").getAsString();
                String rol = user.get("rol").getAsString();
                int id = user.get("id").getAsInt();
                boolean correoVerificado = user.get("correoVerificado").getAsBoolean();
                boolean activo = user.get("activo").getAsBoolean();

                if (!activo) {
                    response.addProperty("Mensaje", "Tu cuenta esta desactivada. Contacta al administrador.");
                    response.addProperty("status", 403);
                    return response;
                }

                if (!correoVerificado) {
                    JsonObject resultado = RecuperacionService.generarTokenVerificacionLogin(correo, id);

                    if (resultado.get("status").getAsInt() == 200) {
                        response.addProperty("Mensaje", "Debe verificar su correo. Te enviamos un nuevo enlace de verificacion.");
                        response.addProperty("correoEnviado", true);
                    } else {
                        response.addProperty("Mensaje", "Debe verificar su correo. No pudimos reenviar el enlace en este momento.");
                        response.addProperty("correoEnviado", false);
                    }
                    response.addProperty("status", 403);
                    return response;
                }

                if (!matchesPassword(contrasena, passwordDb)) {
                    response.addProperty("Mensaje", "La contrasena no coincide con la registrada");
                    response.addProperty("status", 400);
                    return response;
                }

                String token = JwtUtil.generateUserToken(correo, rol, id);
                response.addProperty("Mensaje", "Inicio de sesion correcto");
                response.addProperty("Token", token);
                response.addProperty("status", 200);

            }

            return response;

        } catch (Throwable e) {
            e.printStackTrace();

            response.addProperty("Mensaje", "Servidor ha fallado");
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Compara la contrasena plana contra el hash persistido tolerando prefijos
     * bcrypt legados que la libreria actual no interpreta directamente.
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
