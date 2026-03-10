package com.libraryai.backend;

import com.libraryai.backend.config.AIConfig;
import com.libraryai.backend.seeders.SeedRoles;
import com.libraryai.backend.server.ServerMain;

/**
 * Clase Principal (Entry Point)
 * Orquesta el inicio de los componentes principales: conexión a DB y Servidor
 * Web.
 */
public class App {
    /**
     * Punto de entrada de la aplicacion.
     * Inicia el servidor HTTP y deja el proceso en ejecucion.
     */
    public static void main(String[] args) throws Exception {
        try {
            if (AIConfig.API_KEY == null || AIConfig.API_KEY.isBlank()) {
                System.err.println("Aviso: GEMINI_API_KEY no está configurada. Poly responderá con el fallback local.");
            } else {
                System.out.println("Gemini configurado con modelo: " + AIConfig.MODEL_AI);
            }

            // Inserta los roles en la DB al iniciar el servidor.
            SeedRoles.insertRoles();

            // 1. Iniciar Servidor (Bloqueante)
            ServerMain.startServer();
        }

        catch (ExceptionInInitializerError e) {
            System.err.println(e.getMessage());
        }

    }
}
