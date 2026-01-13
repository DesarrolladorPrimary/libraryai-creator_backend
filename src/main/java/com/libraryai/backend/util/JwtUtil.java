package com.libraryai.backend.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import io.github.cdimascio.dotenv.Dotenv;
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

    public static String validarToken(String token) {
        String very;
        try {
            String validar = Jwts.parser()
                    .verifyWith(keyMaster)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            very = validar;

        } catch (Exception e) {
            e.printStackTrace();
            very = null;
        }

        return very;

    }

}
