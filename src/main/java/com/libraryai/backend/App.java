package com.libraryai.backend;

import com.libraryai.backend.config.ConexionDB;
import com.libraryai.backend.config.ServerMain;

/**
 * Clase Principal (Entry Point)
 * Orquesta el inicio de los componentes principales: conexión a DB y Servidor
 * Web.
 */
public class App {
    public static void main(String[] args) throws Exception {
        // 1. Inicializar Base de Datos
        ConexionDB.getConexion();
        
        // 2. Iniciar Servidor (Bloqueante)
        ServerMain.ServerExect();

    
    }
}
