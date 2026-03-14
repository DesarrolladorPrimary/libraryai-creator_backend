package com.libraryai.backend;

import com.libraryai.backend.config.AIConfig;
import com.libraryai.backend.seeders.SeedAIModels;
import com.libraryai.backend.seeders.SeedForbiddenWords;
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
                System.out.println("Gemini configurado con modelos: gratuito=" + AIConfig.FREE_MODEL
                        + ", premium=" + AIConfig.PREMIUM_MODEL);
            }

            SeedRoles.insertRoles();
            SeedAIModels.insertModels();
            SeedForbiddenWords.insertForbiddenWords();
            ServerMain.startServer();
        } catch (ExceptionInInitializerError e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            System.err.println("Error de inicialización: " + cause.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            throw e;
        }
    }
}
