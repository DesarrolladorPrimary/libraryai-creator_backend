package com.libraryai.backend.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.libraryai.backend.controller.UserController;
import com.sun.net.httpserver.*;

public class ServerMain {

    private static int port = 8080;
    public static HttpServer server;

    public static void ServerExect() throws IOException {
        try {
            System.out.println("\nIniciando servidor...");

            server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Servidor iniciado correctamente en el puerto: " + port + "\n\n");
            server.start();

            UserController.handleUser();

        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}