package com.libraryai.backend.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import com.google.gson.JsonObject;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    private static final String key = Dotenv.load().get("JWT_KEY");
    private static final SecretKey keyMaster;

    static {

        if (key == null) {
            throw new RuntimeException("Llave faltante o no existente");
        }

        keyMaster = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    public static String tokenUsuario(String usuario, String rol) {

        String token = Jwts.builder()
                .issuer("Library-Creator")
                .subject(usuario)
                .claim("role", rol)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(keyMaster)
                .compact();

        return token;
    }

    public static JsonObject validarToken(String token) {
        JsonObject very = new JsonObject();
        try {
            Claims validarClaims = Jwts.parser()
                    .verifyWith(keyMaster)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

                String usuario = validarClaims.getSubject();
                String role = validarClaims.get("role", String.class);

                very.addProperty("Rol", role);
                very.addProperty("Usuario", usuario);
            
        } catch (JwtException e) {
            e.printStackTrace();
            very.addProperty("Mensaje", "El token expiro o ya no funciona");
            very.addProperty("code", 401);
        }

        return very;

    }

}
