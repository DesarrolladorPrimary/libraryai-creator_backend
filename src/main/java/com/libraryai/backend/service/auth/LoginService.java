package com.libraryai.backend.service.auth;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.auth.LoginDao;
import com.libraryai.backend.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;

public class LoginService {

    public static JsonObject verificarDatosLogin(String correo, String contraseña) {

        JsonObject response = new JsonObject();

        if (!correo.matches("[a-zA-Z]+@[a-zA-Z]+\\.[a-zA-Z]{2,6}")) {
            response.addProperty("Mensaje", "El correo no es valido");
            response.addProperty("status", 400);

        } else {
            JsonObject user = LoginDao.validarUsuario(correo);

            int code = user.get("status").getAsInt();

            if (code != 200) {
                response = user;
                return response;
            }

            String contraseñaDB = user.get("contraseña").getAsString();



            
            if (!BCrypt.checkpw(contraseña, contraseñaDB)) {
                response.addProperty("Mensaje", "La contraseña no coincide con la registrada");
                response.addProperty("status", 400);
                return response;
            }   

            String token = JwtUtil.tokenUsuario(correo , "");
            response.addProperty("Mensaje", "Usuario logueado correctamente");
            response.addProperty("Token", token);
            response.addProperty("status", 200);

        }

        return response;
    }
}
