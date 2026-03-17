package com.libraryai.backend;

import com.libraryai.backend.config.AIConfig;
import com.libraryai.backend.dao.SettingsDao;
import com.libraryai.backend.seeders.SeedAIModels;
import com.libraryai.backend.seeders.SeedForbiddenWords;
import com.libraryai.backend.seeders.SeedRoles;
import com.libraryai.backend.server.ServerMain;

/**
 * Punto de entrada del backend.
 *
 * <p>Centraliza la secuencia mínima de arranque: validar la configuración de IA,
 * sincronizar catálogos base en la base de datos y levantar el servidor HTTP.
 */
public class App {
    /**
     * Ejecuta la secuencia de arranque del backend.
     *
     * <p>El orden importa: primero se validan dependencias críticas, luego se
     * garantizan los datos semilla necesarios para que la aplicación responda con
     * un catálogo mínimo coherente.
     */
    public static void main(String[] args) throws Exception {
        try {
            if (AIConfig.API_KEY == null || AIConfig.API_KEY.isBlank()) {
                System.err.println("Aviso: GEMINI_API_KEY no está configurada. Poly responderá con el fallback local.");
            } else {
                System.out.println("Gemini configurado con modelos: gratuito=" + AIConfig.FREE_MODEL
                        + ", premium=" + AIConfig.PREMIUM_MODEL);
            }

            // Los catálogos mínimos se sincronizan antes de aceptar tráfico HTTP.
            SeedRoles.insertRoles();
            SettingsDao.ensureDefaultPlans();
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
