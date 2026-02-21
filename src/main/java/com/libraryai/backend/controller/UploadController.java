package com.libraryai.backend.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.UserDao;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.util.QueryParams;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador para subir archivos (fotos de perfil).
 */
public class UploadController {

    private static final String UPLOAD_DIR = "uploads/perfiles/";

    /**
     * Handler para subir foto de perfil.
     * Ruta: POST /api/v1/upload/perfil?id=X
     * Body esperado: { "imagen": "base64..." }
     */
    public static HttpHandler subirFotoPerfil() {
        return exchange -> {
            System.out.println("\n\nPeticion de tipo: " + exchange.getRequestMethod() + " recibido\n");

            // Obtener ID del usuario
            String query = exchange.getRequestURI().getQuery();
            if (query == null || query.isEmpty()) {
                ApiResponse.error(exchange, 400, "Se requiere el ID del usuario");
                return;
            }

            JsonObject idJson = QueryParams.parseId(query);
            if (idJson.get("status").getAsInt() != 200) {
                ApiResponse.error(exchange, 400, "ID inválido");
                return;
            }
            int usuarioId = idJson.get("id").getAsInt();

            // Leer el body
            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            if (body.isEmpty()) {
                ApiResponse.error(exchange, 400, "No hay cuerpo en la petición");
                return;
            }

            try {
                // Parsear JSON
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(body, JsonObject.class);

                if (!json.has("imagen")) {
                    ApiResponse.error(exchange, 400, "Falta el campo 'imagen'");
                    return;
                }

                String base64Image = json.get("imagen").getAsString();

                // Eliminar prefijo data:image/jpeg;base64, si existe
                if (base64Image.contains(",")) {
                    base64Image = base64Image.split(",")[1];
                }

                // Decodificar base64 a bytes
                byte[] imageData = Base64.getDecoder().decode(base64Image);

                // Validar que sea una imagen
                if (imageData.length == 0) {
                    ApiResponse.error(exchange, 400, "Imagen inválida");
                    return;
                }

                // Crear directorio si no existe
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generar nombre de archivo
                String fileName = "perfil_" + usuarioId + "_" + UUID.randomUUID() + ".jpg";
                Path filePath = uploadPath.resolve(fileName);

                // Guardar archivo
                Files.write(filePath, imageData);
                System.out.println("Imagen guardada en: " + filePath.toAbsolutePath());

                // Guardar ruta en la base de datos
                String rutaImagen = UPLOAD_DIR + fileName;
                System.out.println("Ruta a guardar en DB: " + rutaImagen);
                
                JsonObject result = UserDao.updateFotoPerfil(rutaImagen, usuarioId);

                JsonObject response = new JsonObject();
                if (result.get("status").getAsInt() == 200) {
                    response.addProperty("Mensaje", "Foto actualizada correctamente");
                    response.addProperty("ruta", rutaImagen);
                    ApiResponse.send(exchange, response.toString(), 200);
                } else {
                    ApiResponse.error(exchange, 500, "Error al guardar en DB");
                }

            } catch (IllegalArgumentException e) {
                ApiResponse.error(exchange, 400, "Imagen base64 inválida");
            } catch (Exception e) {
                e.printStackTrace();
                ApiResponse.error(exchange, 500, "Error al procesar imagen: " + e.getMessage());
            }
        };
    }
}
