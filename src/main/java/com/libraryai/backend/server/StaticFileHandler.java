package com.libraryai.backend.server;

import java.io.*;
import java.nio.file.*;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * Handler para servir archivos estáticos (imágenes, css, js, etc.)
 */
public class StaticFileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Obtener la ruta solicitada
        String path = exchange.getRequestURI().getPath();
        
        // Si es la raíz, redirigir o mostrar algo
        if (path.equals("/")) {
            String response = "Servidor funcionando";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        }
        
        // Quitar el slash inicial para buscar el archivo
        String filePath = path.substring(1);
        
        // Buscar el archivo
        Path file = Paths.get(filePath);
        
        if (Files.exists(file) && Files.isRegularFile(file)) {
            // Determinar el tipo de contenido
            String contentType = getContentType(filePath);
            
            // Leer y enviar el archivo
            byte[] fileBytes = Files.readAllBytes(file);
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        } else {
            // Archivo no encontrado
            String response = "404 - File not found";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}
