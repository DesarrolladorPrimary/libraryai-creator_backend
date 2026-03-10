package com.libraryai.backend.service.auth;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.auth.LoginDao;
import com.libraryai.backend.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Servicio de negocio para login.
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

                // Verificar si el correo está verificado
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

                if (!BCrypt.checkpw(contraseña, contraseñaDB)) {
                    response.addProperty("Mensaje", "La contraseña no coincide con la registrada");
                    response.addProperty("status", 400);
                    return response;
                }

                // Genera JWT con correo, rol e id.
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
}
 
