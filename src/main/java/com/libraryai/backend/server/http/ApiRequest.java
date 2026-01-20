package com.libraryai.backend.server.http;

// IOException para manejar errores de lectura
import java.io.IOException;
// InputStream para leer bytes del body de la petición
import java.io.InputStream;
// StandardCharsets para codificación UTF-8
import java.nio.charset.StandardCharsets;

// HttpExchange contiene toda la información de la petición
import com.sun.net.httpserver.HttpExchange;

/**
 * APIREQUEST - Utilidad para leer peticiones HTTP
 *
 * Esta clase encapsula el HttpExchange y proporciona métodos
 * útiles para extraer información de la petición del cliente.
 *
 * Uso:
 * 1. Crear instancia: new ApiRequest(exchange)
 * 2. Leer body: request.readBody()
 *
 * Métodos disponibles:
 * - readBody(): Lee y retorna el body de la petición como String
 */
public class ApiRequest {

    // Almacenamos el HttpExchange para acceder a él en los métodos
    // public para permitir acceso directo si es necesario
    public HttpExchange exchange;

    /**
     * CONSTRUCTOR
     *
     * Crea una nueva instancia de ApiRequest envolviendo un HttpExchange.
     *
     * @param exchange - El HttpExchange de la petición actual
     */
    public ApiRequest(HttpExchange exchange) {
        // Guardamos la referencia al exchange
        this.exchange = exchange;
    }

    /**
     * LEER BODY DE LA PETICIÓN
     *
     * Este método lee todo el contenido del body de la petición
     * y lo retorna como String (útil para peticiones POST, PUT, etc.)
     *
     * @return String con el contenido del body (normalmente JSON)
     * @throws IOException si hay error al leer el body
     *
     * Ejemplo de uso:
     * ApiRequest request = new ApiRequest(exchange);
     * String jsonBody = request.readBody();
     * // jsonBody = "{ \"nombre\": \"Juan\", \"edad\": 25 }"
     */
    public String readBody() throws IOException {

        // Obtenemos el flujo de entrada (InputStream) del body
        // Este stream contiene los bytes que envió el cliente
        InputStream bodyStream = exchange.getRequestBody();

        // Leemos TODOS los bytes del stream
        // y los convertimos a String usando codificación UTF-8
        String body = new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);

        // Retornamos el body como String
        return body;
    }
}
