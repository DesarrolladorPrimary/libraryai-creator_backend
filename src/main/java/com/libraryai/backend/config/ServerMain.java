package com.libraryai.backend.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Servidor Web Básico
 * Implementación cruda de un servidor HTTP utilizando Sockets TCP.
 * 
 * Responsabilidades:
 * 1. Aceptar conexiones entrantes (TCP)
 * 2. Leer y parsear requests HTTP
 * 3. Enrutar peticiones (TODO)
 * 4. Enviar respuestas HTTP
 */

/*
 * Estructura del servidor
 * 1- SocketServer para aceptar conexiones,
 * 2- bucle de manejo de clientes en hilos separados,
 * 3- protocolo de comunicación simple (lectura/escritura de bytes)
 * 4- mecanismos de cierre graceful para evitar fugas de recursos.
 */

public class ServerMain {

    // *Puerto del servidor
    final static int port = 8080;

    // ? Conexion Server-Socket, inicializacion del socket para establecer la
    // conexion con el cliente
    public static void ServerExect() {
        // *Inicializacion del socket en el puerto 8080
        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Servidor funcionando con el socket: " + server);

            // *Bucle de manejo de clientes en un solo hilo
            while (!server.isClosed()) {
                try {
                    // *Espera a que un cliente se conecte, mientras esta en espera (congelado)
                    Socket socket = server.accept();
                    System.out.println("Conexion en el Socket establecido con el cliente: " + socket);

                    /*
                     * Permite crear multiples hilos (threads) dentro del servidor permitiendo
                     * atender multiples clientes al mismo tiempo cada uno con su propio socket
                     */
                    new Thread(() -> {
                        handleInOut(socket);
                    }).start();

                } catch (SocketException e) {
                    // TODO: handle exception
                    System.err.println("Error: Se cerro la conexion inesperadamente. E: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // TODO: handle exception
            System.err.println("Error: El puerto ya esta ocupado. E: " + e.getMessage());
            /*
             * ! Mostrara un mensaje de error avisando que el puerto desigando ya esta
             * ! ocupado por otro programa.
             */
        }
    }

    // ? Metodo encargado de establecer la comunicacion entre el cliente y el
    // servidor, ofreciendo respuestas.
    public static void handleInOut(Socket socket) {
        // * Inicializa los streams: 'in' para recibir peticiones, 'out' para enviar
        // respuestas
        try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {

            // Lee los headers HTTP de la petición del cliente
            String peticion = readHeader(in);
            System.out.println("\nPeticion del usuario: " + peticion);

            // Separa la petición en líneas usando el delimitador CRLF (\r\n)
            // Cada línea representa un header diferente
            String[] partsHeader = peticion.split("\r\n");

            // La primera línea (Request Line) contiene: METODO RUTA PROTOCOLO
            // Ejemplo: "GET /usuarios HTTP/1.1" -> ["GET", "/usuarios", "HTTP/1.1"]
            String[] requestLine = partsHeader[0].split(" ");

            System.out.println("RequestLine = metodo: " + requestLine[0] + " ruta: " + requestLine[1] + " protocolo: "
                    + requestLine[2]);

            // Extrae el método HTTP (GET, POST, PUT, DELETE, etc.)
            String metodo = requestLine[0];
            // Extrae la ruta solicitada (ej: "/", "/usuarios", "/login")
            String ruta = requestLine[1];

            // Variables para construir la respuesta HTTP
            String response = ""; // Respuesta completa (headers + body)
            byte[] bodyBytes = new byte[] {}; // Cuerpo de la respuesta en bytes
            String body; // Cuerpo de la respuesta en texto

            // Verifica si la ruta es "/" y el método es "GET" (página principal)
            if (ruta.equals("/") && metodo.equals("GET")) {

                // Define el cuerpo de la respuesta como JSON
                body = "{ \"Mensaje\" : \"Bienvenido\" }";

                // Convierte el body a bytes usando codificación UTF-8
                bodyBytes = body.getBytes("UTF-8");

                // Construye la respuesta HTTP completa:
                // Línea 1: Protocolo + Código de estado (200 OK = éxito)
                // Content-Type: Indica que el contenido es JSON
                // Content-Length: Tamaño del cuerpo en bytes
                // Connection: close indica que se cerrará la conexión después
                // \r\n\r\n: Línea vacía que separa headers del body
                response = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: Application/json; charset=utf-8\r\n"
                        + "Content-Length: " + bodyBytes.length + "\r\n"
                        + "Connection: close\r\n"
                        + "\r\n"
                        + body;
            }

            // Si la ruta no coincide con ninguna definida, devuelve error 404
            else {
                body = "{ \"Mensaje\" : \"Ruta no encontrada\" }";

                bodyBytes = body.getBytes("UTF-8");

                // Respuesta HTTP con código 404 (Not Found = recurso no existe)
                response = "HTTP/1.1 404 Not Found\r\n"
                        + "Content-Type: Application/json; charset=utf-8\r\n"
                        + "Content-Length: " + bodyBytes.length + "\r\n"
                        + "Connection: close\r\n"
                        + "\r\n"
                        + body;
            }

            // Escribe la respuesta HTTP completa en el stream de salida
            out.write(response.getBytes("UTF-8"));
            // Escribe el cuerpo de la respuesta
            out.write(bodyBytes);
            // flush() fuerza el envío inmediato de los datos al cliente
            out.flush();

            System.out.println("\n//Conexion cerrada con el cliente//\n");
        } catch (IOException e) {
            System.err.println("Error:" + e.getMessage());
        }
    }

    // Método que lee los headers HTTP del InputStream
    // Retorna la petición como String hasta encontrar la línea vacía (\r\n\r\n)
    public static String readHeader(InputStream in) throws IOException {
        // StringBuilder para concatenar los bytes leídos eficientemente
        StringBuilder sb = new StringBuilder();

        // Buffer temporal de 4KB para leer chunks de datos
        byte[] buffer = new byte[4000];

        // Bucle infinito que se rompe cuando termina de leer los headers
        while (true) {
            // Lee bytes del stream y retorna cuántos leyó (-1 si terminó)
            int n = in.read(buffer);

            // Si n es -1, el cliente cerró la conexión, salimos del bucle
            if (n == -1)
                break;

            // Convierte los bytes leídos a String y los agrega al StringBuilder
            sb.append(new String(buffer, 0, n));

            String headersSofar = sb.toString();

            // \r\n\r\n indica el fin de los headers HTTP (línea vacía)
            // Cuando lo encontramos, ya leímos todos los headers
            if (headersSofar.contains("\r\n\r\n"))
                break;

            // Límite de seguridad: máximo 32KB para evitar ataques de memoria
            if (sb.length() > 32 * 1024)
                break;
        }
        return sb.toString();

    }

}
