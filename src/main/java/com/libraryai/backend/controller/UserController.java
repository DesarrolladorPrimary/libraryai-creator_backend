package com.libraryai.backend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.UsuariosDao;
import com.libraryai.backend.server.ServerMain;
import com.libraryai.backend.service.UserService;
import com.sun.net.httpserver.HttpExchange;

public class UserController {

    /**
     * Define el endpoint "/usuarios" y maneja las peticiones HTTP que llegan a él.
     */
    public static void handleUser() throws IOException {

        String path = "/api/v1/usuarios";
        ServerMain.server.createContext(path, exchange -> {

            URI ruta = exchange.getRequestURI();
            String metodo = exchange.getRequestMethod();

            String response = "";

            if (path.equals(ruta.getPath()) || ruta.getPath().startsWith(path)) {
                switch (metodo) {
                    case "GET":
                        if (ruta.toString().contains("id")) {
                            obtenerUsuarioId(exchange);
                        } else {
                            listarUsuarios(exchange);
                        }
                        break;

                    case "POST":
                        crearUsuario(exchange);
                        break;

                    case "DELETE":
                        eliminarUsuario(exchange);
                        break;

                    default:
                        notFoundMetodo(exchange);
                        break;
                }
            } else {
                response = "{ \"Mensaje\" : \"Ruta no encontrada\" }";

                byte[] responseByte = response.getBytes();

                exchange.getResponseHeaders().add("Content-Type", "Application/json; charset_utf-8");
                exchange.sendResponseHeaders(404, responseByte.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseByte);
                    os.close();
                }
            }
        });

    }

    private static void obtenerUsuarioId(HttpExchange exchange) throws IOException {

        JsonObject response = new JsonObject();
        System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

        String parametrosId = exchange.getRequestURI().getQuery();

        String[] parts = parametrosId.split("=");

        int id = Integer.parseInt(parts[parts.length - 1]);
        response = UsuariosDao.buscarPorId(id);

        int statusCode = 200;
        if (response.has("status")) {
            statusCode = response.get("status").getAsInt();
            response.remove("status");
        }

        byte[] responseByte = response.toString().getBytes();

        exchange.getResponseHeaders().add("Content-Type", "Application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseByte.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseByte);
            System.out.println("Conexion cerrada con el cliente\n\n");
            os.close();
        }

    }

    /**
     * Maneja la petición GET para listar usuarios.
     * Obtiene la lista desde el DAO y extrae el código de estado del primer
     * elemento.
     */
    private static void listarUsuarios(HttpExchange exchange) throws IOException {

        System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

        // Llamamos al DAO para que nos de la lista de usuarios (o el error)
        JsonArray response = UsuariosDao.listarTodos();

        // Código de respuesta por defecto: 200 OK
        int statusCode = 200;

        // Verificamos si la lista trae algo
        if (response.size() > 0) {
            // Tomamos el primer elemento para inspeccionar
            JsonObject primObject = response.get(0).getAsJsonObject();

            // Si ese objeto trae la propiedad "status", la usamos como código HTTP
            if (primObject.has("status")) {
                statusCode = primObject.get("status").getAsInt();
            }
        }

        // Convertimos el Array JSON a texto String
        String responseJson = response.toString();

        // Convertimos el texto a bytes para enviarlo por red
        byte[] responseByte = responseJson.getBytes();

        // Configuramos la cabecera para decir que enviamos JSON
        exchange.getResponseHeaders().add("Content-Type", "Application/json; charset=utf-8");
        // Enviamos las cabeceras con el código de estado (200 o 404) y la longitud
        exchange.sendResponseHeaders(statusCode, responseByte.length);
        // Abrimos el canal de salida y escribimos los bytes
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseByte);
            // Mensaje en consola del servidor
            System.out.println("Conexion cerrada con el cliente\n\n");
            // Cerramos la conexión
            os.close();
        }

    }

    /**
     * Maneja la petición POST para crear un nuevo usuario.
     * Lee el JSON del cuerpo, valida los datos y llama al servicio de registro.
     */
    private static void crearUsuario(HttpExchange exchange) throws IOException {

        // Objeto para armar posibles respuestas de error temprano
        JsonObject response = new JsonObject();

        System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

        // Obtenemos el flujo de datos que envió el cliente (el body)
        InputStream bodyCrudo = exchange.getRequestBody();

        // Leemos todos los bytes del body
        byte[] bodyByte = bodyCrudo.readAllBytes();

        // Convertimos los bytes a String usando UTF-8
        String body = new String(bodyByte, StandardCharsets.UTF_8);

        // Si el body está vacío, error 400
        if (body.isEmpty()) {
            response.addProperty("Mensaje", "Los datos estan vacios");

            // Preparación y envío de respuesta de error
            String responseJson = response.toString();
            byte[] responseByte = responseJson.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "Application/json; charset=utf-8");
            exchange.sendResponseHeaders(400, responseByte.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseByte);
                System.out.println("Conexion cerrada con el cliente\n\n");
                os.close();
            }
        }

        // Inicializamos Gson para convertir JSON String a Objetos Java
        Gson gson = new Gson();

        // Convertimos el String 'body' a un JsonObject de Google Gson
        JsonObject json = gson.fromJson(body, JsonObject.class);

        // Extraemos cada campo del JSON
        String nombre = json.get("nombre").getAsString();
        String correo = json.get("correo").getAsString();
        int contraseña = json.get("contraseña").getAsInt();

        System.out.println("Datos recibidos correctamente. \nnombre: " + nombre + ",\ncorreo: " + correo
                + ",\ncontraseña: " + contraseña);

        // Llamamos al servicio 'registUser' para que valide y guarde
        response = UserService.verificarDatosUsuario(nombre, correo, contraseña);

        // Por defecto asumimos que se creó (201)
        int statusCode = 201;
        // Si la respuesta del servicio trae un status específico, lo usamos
        if (response.has("status")) {
            statusCode = response.get("status").getAsInt();
            // Removemos el status del JSON final para no ensuciar la respuesta
            response.remove("status");
        }

        // Convertimos la respuesta final a String
        String responseJson = response.toString();
        byte[] responseByte = responseJson.getBytes();

        // Enviamos cabeceras y contenido
        exchange.getResponseHeaders().add("Content-Type", "Application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseByte.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseByte);
            System.out.println("Conexion cerrada con el cliente\n\n");
            os.close();
        }
    }

    private static void eliminarUsuario(HttpExchange exchange) throws IOException {

        System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

        int id = 0;
        URI rutaDinamica = exchange.getRequestURI();

        String rutaParametros = rutaDinamica.toString();
        System.out.println("Ruta recibida:" + rutaParametros);

        String[] partsRuta = rutaParametros.split("/");

        String[] parametroRuta = partsRuta[partsRuta.length - 1].split("\\?");

        String[] idParametro = parametroRuta[parametroRuta.length - 1].split("=");

        id = Integer.parseInt(idParametro[idParametro.length - 1]);

        JsonObject responseJson = UsuariosDao.eliminarPorId(id);

        int statusCode = 200;
        if (responseJson.has("status")) {
            statusCode = responseJson.get("status").getAsInt();

            responseJson.remove("status");
        }

        String responseString = responseJson.toString();
        byte[] responseByte = responseString.getBytes();

        exchange.getResponseHeaders().add("Content-Type", "Application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseByte.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseByte);
            System.out.println("Conexion cerrada con el cliente\n\n");
            os.close();
        }

    }

    private static void notFoundMetodo(HttpExchange exchange) throws IOException {
        String response = "{ \"Mensaje\" : \"Metodo inexistente\" }";

        byte[] responseByte = response.getBytes();

        exchange.getResponseHeaders().add("Content-Type", "Application/json; charset=utf-8");
        exchange.sendResponseHeaders(405, responseByte.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseByte);
            os.close();
        }
    }
}
