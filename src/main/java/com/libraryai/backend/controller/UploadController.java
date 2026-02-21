package com.libraryai.backend.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * Content-Type: multipart/form-data
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

            // Obtener content-type
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("multipart/form-data")) {
                ApiResponse.error(exchange, 400, "Debe ser multipart/form-data");
                return;
            }

            try {
                // Leer el body
                byte[] bodyBytes = exchange.getRequestBody().readAllBytes();

                // Buscar el boundary
                String boundary = contentType.split("boundary=")[1];

                // Extraer la imagen del body
                String bodyString = new String(bodyBytes);
                String[] parts = bodyString.split("--" + boundary);

                String fileName = "perfil_" + usuarioId + "_" + UUID.randomUUID() + ".jpg";
                byte[] imageData = null;

                for (String part : parts) {
                    if (part.contains("filename=")) {
                        // Encontrar el inicio de los datos de la imagen
                        int headerEnd = part.indexOf("\r\n\r\n");
                        if (headerEnd > 0) {
                            String imageString = part.substring(headerEnd + 4);
                            // Eliminar el último \r\n si existe
                            if (imageString.endsWith("\r\n")) {
                                imageString = imageString.substring(0, imageString.length() - 2);
                            }
                            imageData = imageString.getBytes("ISO-8859-1");
                            break;
                        }
                    }
                }

                if (imageData == null || imageData.length == 0) {
                    ApiResponse.error(exchange, 400, "No se recibió ninguna imagen");
                    return;
                }

                // Crear directorio si no existe
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Guardar archivo
                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, imageData);

                // Guardar ruta en la base de datos
                String rutaImagen = UPLOAD_DIR + fileName;
                JsonObject result = UserDao.updateFotoPerfil(rutaImagen, usuarioId);

                JsonObject response = new JsonObject();
                if (result.get("status").getAsInt() == 200) {
                    response.addProperty("Mensaje", "Foto actualizada correctamente");
                    response.addProperty("ruta", rutaImagen);
                    ApiResponse.send(exchange, response.toString(), 200);
                } else {
                    ApiResponse.error(exchange, 500, "Error al guardar en DB");
                }

            } catch (Exception e) {
                e.printStackTrace();
                ApiResponse.error(exchange, 500, "Error al procesar imagen: " + e.getMessage());
            }
        };
    }
}
