package com.libraryai.backend.service.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.UserDao;
import com.libraryai.backend.dao.auth.RecuperacionDao;
import com.libraryai.backend.dao.auth.LoginDao;

/**
 * Servicio de recuperación de contraseña.
 */
public class RecuperacionService {

    /**
     * Solicitar recuperación de contraseña.
     * Busca usuario por correo, genera token y lo guarda en la DB.
     * Por ahora, el link se muestra en la consola del servidor.
     * 
     * @param correo Correo del usuario que quiere recuperar contraseña.
     * @return JsonObject con el resultado de la operación.
     */
    public static JsonObject solicitarRecuperacion(String correo) {
        // Objeto JSON para la respuesta
        JsonObject response = new JsonObject();
        
        // Busca el usuario en la DB por correo
        JsonObject usuario = LoginDao.findUserByEmail(correo);
        
        // Si el usuario no existe, responde con error
        if (usuario.has("status") && usuario.get("status").getAsInt() != 200) {
            response.addProperty("Mensaje", "Usuario no encontrado");
            response.addProperty("status", 404);
            return response;
        }
        
        // Obtiene el ID del usuario encontrado
        int usuarioId = usuario.get("id").getAsInt();
        
        // Genera un token único usando UUID
        String token = UUID.randomUUID().toString();
        
        // Define la expiración del token (1 hora desde ahora)
        LocalDateTime expiracion = LocalDateTime.now().plusHours(1);
        
        // Guarda el token en la base de datos
        RecuperacionDao.guardarToken(usuarioId, token, expiracion);
        
        // Por ahora, muestra el link en la consola del servidor
        // (después se enviará por correo electrónico)
        String link = "http://localhost:8080/api/v1/recuperar/validar?token=" + token;
        System.out.println("\n========== LINK DE RECUPERACIÓN ==========");
        System.out.println("Para recuperar contraseña, ingresa a:");
        System.out.println(link);
        System.out.println("============================================\n");
        
        // Responde al cliente
        response.addProperty("Mensaje", "Se ha enviado un enlace a tu correo");
        response.addProperty("status", 200);
        
        return response;
    }

    /**
     * Validar token de recuperación.
     * Verifica que el token exista, no haya sido usado y no haya expirado.
     * 
     * @param token Token de recuperación a validar.
     * @return JsonObject con el resultado de la validación.
     */
    public static JsonObject validarToken(String token) {
        // Delega la validación al DAO
        return RecuperacionDao.validarToken(token);
    }

    /**
     * Establecer nueva contraseña.
     * Valida el token, actualiza la contraseña y marca el token como usado.
     * 
     * @param token Token de recuperación.
     * @param nuevaContraseña Nueva contraseña sin hashear.
     * @return JsonObject con el resultado de la operación.
     */
    public static JsonObject nuevaPassword(String token, String nuevaContraseña) {
        // Objeto JSON para la respuesta
        JsonObject response = new JsonObject();
        
        // Primero valida el token
        JsonObject validacion = RecuperacionDao.validarToken(token);
        
        // Si el token no es válido, retorna el error
        if (validacion.has("status") && validacion.get("status").getAsInt() != 200) {
            return validacion;
        }
        
        // Obtiene el ID del usuario y del token
        int usuarioId = validacion.get("usuarioId").getAsInt();
        int tokenId = validacion.get("tokenId").getAsInt();
        
        // Hashea la nueva contraseña antes de guardarla
        String nuevoHash = BCrypt.hashpw(nuevaContraseña, BCrypt.gensalt());
        
        // Actualiza la contraseña en la base de datos
        boolean actualizado = RecuperacionDao.actualizarPassword(usuarioId, nuevoHash);
        
        // Si la actualización fue exitosa
        if (actualizado) {
            // Marca el token como usado para que no se pueda volver a usar
            RecuperacionDao.marcarTokenUsado(tokenId);
            
            response.addProperty("Mensaje", "Contraseña actualizada correctamente");
            response.addProperty("status", 200);
        } else {
            response.addProperty("Mensaje", "Error al actualizar contraseña");
            response.addProperty("status", 500);
        }
        
        return response;
    }
}
