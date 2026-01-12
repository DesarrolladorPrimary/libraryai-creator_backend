package com.libraryai.backend.server;

// IOException para manejar errores de red/conexión
import java.io.IOException;
// InetSocketAddress para especificar IP y puerto del servidor
import java.net.InetSocketAddress;

// Importamos nuestra clase de rutas
import com.libraryai.backend.router.Rutas;
// Clases del servidor HTTP de Java
import com.sun.net.httpserver.*;

/**
 * SERVERMAIN - El punto de inicio del servidor HTTP
 * 
 * Esta clase se encarga de:
 * 1. Crear el servidor HTTP en un puerto específico
 * 2. Conectar el Router para manejar las peticiones
 * 3. Iniciar el servidor para que escuche peticiones
 */
public class ServerMain {

    // Puerto donde escuchará el servidor (localhost:8080)
    private static int port = 8080;

    // Instancia del servidor HTTP (pública para acceder desde otros lugares si es
    // necesario)
    public static HttpServer server;

    /**
     * MÉTODO PRINCIPAL QUE INICIA EL SERVIDOR
     * 
     * Flujo:
     * 1. Crea el servidor en el puerto 8080
     * 2. Inicia el servidor
     * 3. Configura las rutas
     * 4. Conecta el Router al servidor
     */
    public static void ServerExect() throws IOException {
        try {
            System.out.println("\nIniciando servidor...");

            // Creamos el servidor HTTP
            // InetSocketAddress(port) = escucha en todas las IPs en el puerto 8080
            // El 0 es el backlog (cola de conexiones pendientes, 0 = valor por defecto)
            server = HttpServer.create(new InetSocketAddress(port), 0);

            System.out.println("Servidor iniciado correctamente en el puerto: " + port + "\n\n");

            // Iniciamos el servidor (empieza a escuchar peticiones)
            server.start();

            // ========== CONFIGURACIÓN DE RUTAS ==========

            // Creamos una instancia de nuestra clase de Rutas
            Rutas rutas = new Rutas();

            // Llamamos al método que registra todas las rutas
            // y nos devuelve el Router configurado
            HttpHandler router = rutas.ruts();

            // Conectamos el Router al servidor
            // "/" significa que el Router manejará TODAS las rutas que empiecen con /
            // Es decir, todas las peticiones pasan por nuestro Router
            server.createContext("/", router);

        } catch (IOException e) {
            // Si hay error al crear el servidor, lo mostramos
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}