package com.libraryai.backend.server.http;

// IOException para manejar errores de escritura
import java.io.IOException;
// OutputStream para enviar bytes al cliente
import java.io.OutputStream;

// JsonObject para construir respuestas JSON
import com.google.gson.JsonObject;
// HttpExchange contiene la petición y permite enviar respuesta
import com.sun.net.httpserver.HttpExchange;

/**
 * APIRESPONSE - Utilidad para enviar respuestas HTTP
 *
 * Esta clase simplifica el proceso de enviar respuestas JSON al cliente.
 * En lugar de escribir todo el código de envío cada vez, usamos estos métodos.
 *
 * Métodos disponibles:
 * - send(): Envía cualquier respuesta con código personalizado
 * - success(): Envía respuesta exitosa (código 200)
 * - error(): Envía respuesta de error con código y mensaje personalizados
 */
public class ApiResponse {

    /**
     * MÉTODO BASE: Enviar respuesta al cliente
     *
     * Este es el método principal que usan los demás.
     * Configura las cabeceras y envía el body al cliente.
     *
     * @param exchange   - El objeto HttpExchange de la petición actual
     * @param body       - El contenido a enviar (String, normalmente JSON)
     * @param statusCode - Código HTTP (200=OK, 404=Not Found, 500=Error, etc.)
     */
    public static void send(HttpExchange exchange, String body, int statusCode) throws IOException {

        // Convertimos el String a bytes para enviar por red
        byte[] bodyByte = body.getBytes();

        // Agregamos la cabecera Content-Type para indicar que enviamos JSON
        exchange.getResponseHeaders().add("Content-Type", "Application/json; charset=utf-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET ,POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");


        // Enviamos las cabeceras HTTP:
        // - statusCode: código de respuesta (200, 404, 500, etc.)
        // - bodyByte.length: longitud del body en bytes
        exchange.sendResponseHeaders(statusCode, bodyByte.length);

        // Abrimos el canal de salida y escribimos los bytes
        // try-with-resources: cierra automáticamente el stream
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bodyByte);
            os.close();
        }
    }

    /**
     * MÉTODO RÁPIDO: Enviar respuesta exitosa (200 OK)
     *
     * Usa este método cuando la operación fue exitosa.
     *
     * @param exchange - El objeto HttpExchange de la petición actual
     * @param body     - El contenido JSON a enviar
     */
    public static void success(HttpExchange exchange, String body) throws IOException {
        // Código 200 = OK (operación exitosa)
        int statusCode = 200;

        // Cuerpo basico de confirmacion.
        JsonObject json = new JsonObject();

        json.addProperty("Mensaje", body);

        String bodyJson = json.toString();

        // Delegamos al método send()
        send(exchange, bodyJson, statusCode);
    }

    /**
     * Envía una respuesta 201 con mensaje de creacion generico.
     */
    public static void created(HttpExchange exchange, int code) throws IOException{

        // Cuerpo basico de confirmacion.
        JsonObject json = new JsonObject();

        json.addProperty("Mensaje", "User creado exitosamente");

        String body = json.toString();

        send(exchange, body, 201);
    }




    /**
     * MÉTODO RÁPIDO: Enviar respuesta de error
     *
     * Usa este método cuando ocurre un error.
     * Automáticamente construye un JSON con formato: { "Error": "mensaje" }
     *
     * @param exchange - El objeto HttpExchange de la petición actual
     * @param code     - Código HTTP de error (400, 404, 500, etc.)
     * @param message  - Mensaje descriptivo del error
     *
     * Códigos comunes:
     * - 400: Bad Request (datos incorrectos del cliente)
     * - 404: Not Found (recurso no encontrado)
     * - 500: Internal Server Error (error del servidor)
     */
    public static void error(HttpExchange exchange, int code, String message) throws IOException {
        // Creamos un objeto JSON para la respuesta de error
        JsonObject response = new JsonObject();

        // Agregamos la propiedad "Error" con el mensaje
        response.addProperty("Error", message);

        // Convertimos a String
        String body = response.toString();

        // Enviamos usando el método send()
        send(exchange, body, code);
    }

}
