package com.libraryai.backend.service.auth;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.auth.LoginDao;
import com.libraryai.backend.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Servicio de negocio para login.
 */
public class LoginService {

    /**
     * Valida formato de correo, verifica credenciales y genera token JWT.
     */
    public static JsonObject validateLoginData(String correo, String contraseña) {
        
        JsonObject response = new JsonObject();
        try {


            if (!correo.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9._%+-]+\\.[a-zA-Z]{2,100}")) {
                response.addProperty("Mensaje", "El correo no es valido");
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

                if (!BCrypt.checkpw(contraseña, contraseñaDB)) {
                    response.addProperty("Mensaje", "La contraseña no coincide con la registrada");
                    response.addProperty("status", 400);
                    return response;
                }

                // Genera JWT con correo, rol e id.
                String token = JwtUtil.generateUserToken(correo, rol, id);
                response.addProperty("Mensaje", "Usuario logueado correctamente");
                response.addProperty("Token", token);
                response.addProperty("status", 200);

            }

            return response;

        } catch (Throwable e) {
            e.printStackTrace();

            response.addProperty("Error" , "Servidor ha fallado");
            response.addProperty("status", 500);
            return response;
        }
    }
}
 