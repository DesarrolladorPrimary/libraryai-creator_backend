package com.libraryai.backend.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.management.RuntimeErrorException;

import com.google.gson.JsonObject;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidades para crear y validar JWT.
 */
public class JwtUtil {

    private static final String key = Dotenv.load().get("JWT_KEY");
    private static final SecretKey keyMaster;

    static {

        // Valida que exista la llave en el entorno antes de construir el SecretKey.
        if (key == null) {
            throw new RuntimeErrorException(null, "Llave faltante o no existente");
        }

        // Convierte la llave en un SecretKey compatible con JWT HMAC.
        keyMaster = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un JWT firmado con rol e id embebidos como claims.
     * El token expira a la hora de su emision.
     */
    public static String generateUserToken(String usuario, String rol, int id) {
        try {
        // Construye el JWT con issuer, subject y claims custom.
        String token = Jwts.builder()
                .issuer("Library-Creator")
                .subject(usuario)
                .claim("role", rol)
                .claim("id", id)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(keyMaster)
                .compact();

        return token;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Valida un JWT y devuelve claims basicos en un JsonObject.
     * Si falla, retorna un objeto con Mensaje y code.
     */
    public static JsonObject validateToken(String token) {
        JsonObject very = new JsonObject();
        try {
            // Parsea y verifica la firma del token.
            Claims validarClaims = Jwts.parser()
                    .verifyWith(keyMaster)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

                // Extrae claims y los expone con nombres legibles.
                String usuario = validarClaims.getSubject();
                String role = validarClaims.get("role", String.class);
                double id = validarClaims.get("id", Double.class);

                very.addProperty("Rol", role);
                very.addProperty("Usuario", usuario);
                very.addProperty("Id", id);
            
        } catch (JwtException e) {
            e.printStackTrace();
            // Marca el error con mensaje y codigo para que el caller responda 401.
            very.addProperty("Mensaje", "El token expiro o ya no funciona");
            very.addProperty("code", 401);
        }

        return very;

    }

}
