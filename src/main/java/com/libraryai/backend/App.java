package com.libraryai.backend;

import com.libraryai.backend.config.AIConfig;
import com.libraryai.backend.seeders.SeedRoles;
import com.libraryai.backend.server.ServerMain;

/**
 * Clase principal de la aplicación.
 */
public class App {
    /**
     * Punto de entrada de la aplicación.
     */
    public static void main(String[] args) throws Exception {
        try {
            if (AIConfig.API_KEY == null || AIConfig.API_KEY.isBlank()) {
                System.err.println("Aviso: GEMINI_API_KEY no está configurada. Poly responderá con el fallback local.");
            } else {
                System.out.println("Gemini configurado con modelo: " + AIConfig.MODEL_AI);
            }

            SeedRoles.insertRoles();
            ServerMain.startServer();
        } catch (ExceptionInInitializerError e) {
            System.err.println(e.getMessage());
        }
    }
}
