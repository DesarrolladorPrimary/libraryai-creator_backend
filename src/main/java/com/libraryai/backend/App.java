package com.libraryai.backend;

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
