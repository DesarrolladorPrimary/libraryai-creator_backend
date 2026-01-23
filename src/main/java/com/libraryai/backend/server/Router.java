package com.libraryai.backend.server;

// Importamos IOException para manejar errores de entrada/salida
import java.io.IOException;
// HashMap es la implementación de Map que usaremos para almacenar rutas
import java.util.HashMap;
// Map es la interfaz para estructuras clave-valor
import java.util.Map;

// Importamos nuestra clase para enviar respuestas de error
import com.libraryai.backend.server.http.ApiResponse;
// Importamos las clases del servidor HTTP de Java
import com.sun.net.httpserver.*;

/**
 * ROUTER - El "recepcionista" del servidor
 *
 * Esta clase se encarga de:
 * 1. Almacenar todas las rutas registradas (GET /usuarios, POST /login, etc.)
 * 2. Cuando llega una petición, buscar qué handler debe procesarla
 * 3. Si no existe la ruta, responder con error 404
 *
 * Implementa HttpHandler para poder recibir peticiones HTTP del servidor
 */
public class Router implements HttpHandler {

    /**
     * ESTRUCTURA DE ALMACENAMIENTO DE RUTAS
     *
     * Es un Map anidado (Map dentro de Map):
     * - Primera clave: método HTTP ("GET", "POST", "DELETE", etc.)
     * - Segunda clave: path de la ruta ("/api/v1/usuarios", "/login", etc.)
     * - Valor final: el HttpHandler que procesa esa ruta
     *
     * Ejemplo de cómo se ve internamente:
     * {
     * "GET" -> { "/api/v1/usuarios" -> listarUsuariosHandler,
     * "/api/v1/usuarios/id" -> obtenerUsuarioHandler },
     * "POST" -> { "/api/v1/usuarios" -> crearUsuarioHandler },
     * "DELETE" -> { "/api/v1/usuarios" -> eliminarUsuarioHandler }
     * }
     */
    Map<String, Map<String, HttpHandler>> router = new HashMap<>();

    /**
     * MÉTODO PARA REGISTRAR RUTAS
     *
     * @param method - El método HTTP (ej: "GET", "POST", "DELETE")
     * @param path   - La ruta/endpoint (ej: "/api/v1/usuarios")
     * @param handle - El handler que procesará las peticiones a esta ruta
     */
    public void addroute(String method, String path, HttpHandler handle) {
        // computeIfAbsent: Si no existe un Map para este método, lo crea
        // Ejemplo: Si es la primera ruta GET, crea el Map interno para GET
        Map<String, HttpHandler> map = router.computeIfAbsent(method, router -> new HashMap<>());

        // Agrega la ruta y su handler al Map interno
        // Ejemplo: agrega "/api/v1/usuarios" -> listarUsuariosHandler
        map.put(path, handle);
    }

    /**
     * Registra una ruta GET.
     */
    public void get(String path, HttpHandler handle) {
        addroute("GET", path, handle);
    }

    /**
     * Registra una ruta POST.
     */
    public void post(String path, HttpHandler handle) {
        addroute("POST", path, handle);
    }

    /**
     * Registra una ruta PUT.
     */
    public void put(String path, HttpHandler handle) {
        addroute("PUT", path, handle);

    }

    /**
     * Registra una ruta DELETE.
     */
    public void delete(String path, HttpHandler handle) {
        addroute("DELETE", path, handle);
    }


    public void options(String path, HttpHandler handle){
        addroute("OPTIONS", path, handle);

    }
    /**
     * MÉTODO QUE PROCESA CADA PETICIÓN HTTP
     *
     * Este método se ejecuta automáticamente cuando llega una petición al servidor.
     * Es obligatorio implementarlo porque Router implementa HttpHandler.
     *
     * @param exchange - Contiene toda la información de la petición y permite
     * enviar respuesta
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Extraemos el método HTTP de la petición (GET, POST, DELETE, etc.)
        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("OPTIONS")) {
            ApiResponse.send(exchange, "", 200);
            return;
        }
        // Extraemos el path/ruta de la petición (ej: "/api/v1/usuarios")
        String path = exchange.getRequestURI().getPath();

        // Buscamos si existe un Map de rutas para este método HTTP
        // Ejemplo: buscamos todas las rutas GET registradas
        Map<String, HttpHandler> metodos = router.get(method);

        // Verificamos si encontramos rutas para este método
        if (metodos != null) {
            // Buscamos el handler específico para este path
            // Ejemplo: buscamos el handler para "/api/v1/usuarios"
            HttpHandler handler = metodos.get(path);

            // Si encontramos el handler, lo ejecutamos
            if (handler != null) {
                // Delegamos el procesamiento al handler correspondiente
                // Este handler es el que definimos en UserController
                handler.handle(exchange);
            } else {
                // El método existe (ej: GET) pero la ruta no (ej: /ruta-inexistente)
                String error = "No encontrado";
                ApiResponse.error(exchange, 404, error);
            }
        } else {
            // El método HTTP no tiene ninguna ruta registrada
            String error = "No encontrado";
            ApiResponse.error(exchange, 404, error);
        }

    }

}
