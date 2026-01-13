package com.libraryai.backend.controller;

// InputStream para leer el body de las peticiones POST
import java.io.InputStream;
// URI para manejar rutas dinámicas con parámetros
import java.net.URI;
// StandardCharsets para codificación UTF-8
import java.nio.charset.StandardCharsets;

// Gson es la librería de Google para manejar JSON
import com.google.gson.Gson;
// JsonArray para listas de objetos JSON (ej: lista de usuarios)
import com.google.gson.JsonArray;
// JsonObject para objetos JSON individuales
import com.google.gson.JsonObject;
// DAO (Data Access Object) para operaciones con la base de datos
import com.libraryai.backend.dao.UsuariosDao;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
// Servicio para lógica de negocio de usuarios
import com.libraryai.backend.service.UserService;
// HttpHandler es la interfaz que retornamos para el Router
import com.sun.net.httpserver.HttpHandler;

/**
 * USERCONTROLLER - Controlador de la entidad Usuario
 * 
 * Este controlador maneja todas las operaciones HTTP relacionadas con usuarios:
 * - GET: Listar usuarios, obtener usuario por ID
 * - POST: Crear nuevo usuario
 * - DELETE: Eliminar usuario
 * 
 * Cada método público retorna un HttpHandler que el Router usa para procesar
 * peticiones.
 * 
 * PATRÓN USADO: Cada método retorna un lambda (exchange -> { ... })
 * Este lambda es el código que se ejecuta cuando llega una petición a esa ruta.
 */
public class UserController {

    /**
     * HANDLER: Obtener usuario por ID
     * 
     * Ruta: GET /api/v1/usuarios/id?id=X
     * Ejemplo: GET /api/v1/usuarios/id?id=5
     * 
     * @return HttpHandler que busca un usuario por su ID y retorna sus datos
     */
    public static HttpHandler obtenerUsuarioId() {

        // Retornamos un lambda que procesa la petición
        return exchange -> {
            // Objeto para construir la respuesta JSON
            JsonObject response = new JsonObject();

            // Log en consola del servidor
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

            // Obtenemos los parámetros de la URL (lo que viene después del ?)
            // Ejemplo: "id=5" de "/api/v1/usuarios/id?id=5"
            String parametrosId = exchange.getRequestURI().getQuery();

            // Separamos por "=" para obtener el valor
            // ["id", "5"] -> tomamos el último elemento
            String[] parts = parametrosId.split("=");

            // Convertimos el ID de String a int
            int id = Integer.parseInt(parts[parts.length - 1]);

            // Llamamos al DAO para buscar el usuario en la base de datos
            response = UsuariosDao.buscarPorId(id);

            // Código HTTP por defecto: 200 OK
            int statusCode = 200;

            // Si el DAO devuelve un status específico (ej: 404 si no existe), lo usamos
            if (response.has("status")) {
                statusCode = response.get("status").getAsInt();
                // Removemos el status del JSON para no mostrarlo al cliente
                response.remove("status");
            }

            String responsString = response.toString();
            ApiResponse.send(exchange, responsString, statusCode);
        };
    }

    /**
     * HANDLER: Listar todos los usuarios
     * 
     * Ruta: GET /api/v1/usuarios
     * 
     * @return HttpHandler que obtiene la lista completa de usuarios de la BD
     */
    public static HttpHandler listarUsuarios() {
        return exchange -> {
            // Imprime en consola el método HTTP recibido para logging
            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

            // Llama al DAO para obtener todos los usuarios de la base de datos
            // Retorna un JsonArray con la lista de objetos JSON de usuarios
            JsonArray response = UsuariosDao.listarTodos();

            // Código HTTP por defecto: 200 OK (éxito)
            int statusCode = 200;

            // Verifica si la respuesta contiene elementos
            if (response.size() > 0) {
                // Obtiene el primer elemento para verificar si es un mensaje de error
                JsonObject primObject = response.get(0).getAsJsonObject();

                // Si el primer objeto tiene "status", indica que es un mensaje de error
                if (primObject.has("status")) {
                    // Extrae el código de estado del mensaje de error
                    statusCode = primObject.get("status").getAsInt();
                }
            }

            // Convierte el JsonArray a String para enviar como respuesta JSON
            String responseJson = response.toString();

            // Envía la respuesta al cliente con el código HTTP determinado
            ApiResponse.send(exchange, responseJson, statusCode);

        };

    }

    /**
     * HANDLER: Crear nuevo usuario
     * 
     * Ruta: POST /api/v1/usuarios
     * Body esperado: { "nombre": "...", "correo": "...", "contraseña": ... }
     * 
     * @return HttpHandler que crea un nuevo usuario con los datos del body
     */
    public static HttpHandler crearUsuario() {

        return exchange -> {
            // Objeto para construir respuestas
            JsonObject response = new JsonObject();

            System.out.println("Peticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

            // ========== LECTURA DEL BODY ==========

            // Obtenemos el flujo de entrada (el body que envió el cliente)
            InputStream bodyCrudo = exchange.getRequestBody();

            // Leemos todos los bytes del body
            byte[] bodyByte = bodyCrudo.readAllBytes();

            // Convertimos los bytes a String usando UTF-8
            String body = new String(bodyByte, StandardCharsets.UTF_8);

            // ========== VALIDACIÓN ==========

            // Si el body está vacío, respondemos error 400 Bad Request
            if (body.isEmpty()) {

                String responseJson = response.toString();
                ApiResponse.error(exchange, 400, "Los datos estan vacios");
                return; // Importante: salimos del handler aquí
            }

            // ========== PARSEO DEL JSON ==========

            // Gson convierte String JSON a objetos Java
            Gson gson = new Gson();

            // Parseamos el body a un JsonObject
            JsonObject json = gson.fromJson(body, JsonObject.class);

            // Extraemos cada campo del JSON recibido
            String nombre = json.get("nombre").getAsString();
            String correo = json.get("correo").getAsString();
            int contraseña = json.get("contraseña").getAsInt();

            System.out.println("Datos recibidos correctamente. \nnombre: " + nombre + ",\ncorreo: " + correo
                    + ",\ncontraseña: " + contraseña);

            // ========== LÓGICA DE NEGOCIO ==========

            // Llamamos al servicio para validar y guardar el usuario
            response = UserService.verificarDatosUsuario(nombre, correo, contraseña);

            // Código HTTP por defecto: 201 Created (recurso creado)
            int statusCode = 201;

            // Si el servicio devuelve un status específico, lo usamos
            if (response.has("status")) {
                statusCode = response.get("status").getAsInt();
                response.remove("status");
            }

            // ========== ENVÍO DE RESPUESTA ==========

            String responseJson = response.toString();
            ApiResponse.send(exchange, responseJson, statusCode);
        };
    }

    public static HttpHandler actualizarUsuario() {
        return exchange -> {
            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

            ApiRequest request = new ApiRequest(exchange);

            String body = request.readBody();

            if (body.isEmpty()) {
                ApiResponse.error(exchange, 400, "No hay cuerpo");
            }

            URI path = exchange.getRequestURI();
            String parametroId = path.getQuery();

            if (parametroId.isEmpty() || parametroId == null) {
                ApiResponse.error(exchange, 400, "Parametros en la url faltantes");
            }

            String[] parts = parametroId.split("=");

            int id = Integer.parseInt(parts[parts.length - 1]);

            if (id < 1) {
                ApiResponse.error(exchange, 400, "El id no es valido");
                return;
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);

            String nombre = "";
            String correo = "";
            int contraseña = 0;

            
            if (json.has("nombre")) {
                nombre = json.get("nombre").getAsString();
            }
            
            if (json.has("correo")) {
                correo = json.get("correo").getAsString();
            }
            
            if (json.has("contraseña")) {
                contraseña = json.get("contraseña").getAsInt();
            }
            
            JsonObject response = UserService.verificarDatosActualizar(nombre, correo, contraseña, id);

            int code = response.get("status").getAsInt();

            response.remove("status");

            String responseJson = response.toString();
            ApiResponse.send(exchange, responseJson, code);

        };
    }

    /**
     * HANDLER: Eliminar usuario por ID
     * 
     * Ruta: DELETE /api/v1/usuarios?id=X
     * Ejemplo: DELETE /api/v1/usuarios?id=5
     * 
     * @return HttpHandler que elimina un usuario de la base de datos
     */
    public static HttpHandler eliminarUsuario() {

        return exchange -> {
            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido del cliente\n");

            // Variable para almacenar el ID a eliminar
            int id = 0;

            // Obtenemos la URI completa de la petición
            URI rutaDinamica = exchange.getRequestURI();

            // Convertimos la URI a String para procesarla
            String rutaParametros = rutaDinamica.toString();
            System.out.println("Ruta recibida:" + rutaParametros);

            // ========== EXTRACCIÓN DEL ID ==========
            // Proceso: "/api/v1/usuarios?id=5"

            // Separamos por "/" -> ["", "api", "v1", "usuarios?id=5"]
            String[] partsRuta = rutaParametros.split("/");

            // Tomamos el último elemento y separamos por "?"
            // "usuarios?id=5" -> ["usuarios", "id=5"]
            String[] parametroRuta = partsRuta[partsRuta.length - 1].split("\\?");

            // Tomamos el último y separamos por "="
            // "id=5" -> ["id", "5"]
            String[] idParametro = parametroRuta[parametroRuta.length - 1].split("=");

            // Tomamos el valor del ID y lo convertimos a int
            id = Integer.parseInt(idParametro[idParametro.length - 1]);

            // ========== ELIMINACIÓN EN BD ==========

            // Llamamos al DAO para eliminar el usuario
            JsonObject responseJson = UsuariosDao.eliminarPorId(id);

            // Código HTTP por defecto: 200 OK
            int statusCode = 200;

            // Si el DAO devuelve un status específico, lo usamos
            if (responseJson.has("status")) {
                statusCode = responseJson.get("status").getAsInt();
                responseJson.remove("status");
            }

            // ========== ENVÍO DE RESPUESTA ==========

            String responseString = responseJson.toString();
            ApiResponse.send(exchange, responseString, statusCode);
        };
    }

}
