package com.libraryai.backend.service.auth;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.auth.LoginDao;
import com.libraryai.backend.util.JwtUtil;

public class LoginService {

    public static JsonObject verificarDatosLogin(String correo, int contraseña) {

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

            int contraseñaDB = user.get("contraseña").getAsInt();

            if (contraseñaDB != contraseña) {
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
