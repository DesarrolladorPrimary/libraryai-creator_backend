package com.libraryai.backend.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.libraryai.backend.routes.Routes;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Punto de inicio del servidor HTTP.
 */
public class ServerMain {

    private static final Dotenv ENV = Dotenv.load();
    private static final int DEFAULT_PORT = 8080;
    private static final int port = resolvePort();

    public static HttpServer server;

    public static void startServer() throws IOException {
        try {
            System.out.println("\nIniciando servidor...");

            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            System.out.println("Servidor iniciado correctamente en el puerto: " + port + "\n\n");

            server.start();

            Routes rutas = new Routes();
            HttpHandler router = rutas.configureRoutes();

            server.createContext("/", router);
            server.createContext("/uploads/", new StaticFileHandler());

        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            throw e;
        }
    }

    private static int resolvePort() {
        String configuredPort = ENV.get("SERVER_PORT");

        if (configuredPort == null || configuredPort.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(configuredPort.trim());
        } catch (NumberFormatException error) {
            System.err.println("SERVER_PORT inválido. Usando puerto por defecto: " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }
}
