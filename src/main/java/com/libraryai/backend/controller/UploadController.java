package com.libraryai.backend.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.libraryai.backend.dao.UploadedFileDao;
import com.libraryai.backend.dao.UserDao;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.StoryService;
import com.libraryai.backend.util.JwtUtil;
import com.libraryai.backend.util.QueryParams;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador para subir archivos (fotos de perfil).
 */
public class UploadController {

    private static final String UPLOAD_DIR = "uploads/perfiles/";
    private static final String STORY_UPLOAD_DIR = "uploads/relatos/";
    private static final long MAX_STORY_FILE_SIZE = 10L * 1024L * 1024L;

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

    /**
     * Handler para subir archivo fuente de un relato.
     * Ruta: POST /api/v1/upload/relato?storyId=X
     * Body esperado: { "archivo": "base64...", "nombreArchivo": "...", "mimeType": "..." }
     */
    public static HttpHandler subirArchivoRelato() {
        return exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Integer storyId = extractNumericQuery(query, "storyId");
            int userId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));

            if (storyId == null || storyId <= 0) {
                ApiResponse.error(exchange, 400, "Se requiere storyId");
                return;
            }

            if (userId <= 0) {
                ApiResponse.error(exchange, 401, "Usuario no autenticado");
                return;
            }

            JsonObject accessValidation = StoryService.validateStoryOwnership(storyId, userId);
            if (accessValidation != null) {
                ApiResponse.send(exchange, accessValidation.toString(), accessValidation.get("status").getAsInt());
                return;
            }

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();
            if (body.isEmpty()) {
                ApiResponse.error(exchange, 400, "No hay cuerpo en la petición");
                return;
            }

            try {
                JsonObject payload = new Gson().fromJson(body, JsonObject.class);
                String fileName = payload != null && payload.has("nombreArchivo")
                        ? payload.get("nombreArchivo").getAsString()
                        : null;
                String mimeType = payload != null && payload.has("mimeType")
                        ? payload.get("mimeType").getAsString()
                        : "";
                String base64File = payload != null && payload.has("archivo")
                        ? payload.get("archivo").getAsString()
                        : null;

                if (fileName == null || fileName.trim().isEmpty() || base64File == null || base64File.trim().isEmpty()) {
                    ApiResponse.error(exchange, 400, "Faltan datos del archivo");
                    return;
                }

                String fileType = resolveStoryFileType(fileName, mimeType);
                if (fileType == null) {
                    ApiResponse.error(exchange, 400, "Solo se permiten archivos PDF, DOC o DOCX");
                    return;
                }

                if (base64File.contains(",")) {
                    base64File = base64File.split(",")[1];
                }

                byte[] fileBytes = Base64.getDecoder().decode(base64File);
                if (fileBytes.length <= 0) {
                    ApiResponse.error(exchange, 400, "Archivo inválido");
                    return;
                }

                if (fileBytes.length > MAX_STORY_FILE_SIZE) {
                    ApiResponse.error(exchange, 400, "El archivo no puede superar 10 MB");
                    return;
                }

                JsonObject quotaValidation = StoryService.validateStorageQuota(userId, fileBytes.length,
                        "subir archivos fuente a tu relato");
                if (quotaValidation != null) {
                    ApiResponse.send(exchange, quotaValidation.toString(), quotaValidation.get("status").getAsInt());
                    return;
                }

                Path uploadPath = Paths.get(STORY_UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String extension = getFileExtension(fileName);
                String storedName = "relato_" + storyId + "_" + UUID.randomUUID() + extension;
                Path filePath = uploadPath.resolve(storedName);
                Files.write(filePath, fileBytes);

                String relativePath = STORY_UPLOAD_DIR + storedName;
                JsonObject createFile = UploadedFileDao.create(userId, fileName.trim(), fileType, "Subido",
                        relativePath, fileBytes.length);

                if (!createFile.has("status") || createFile.get("status").getAsInt() != 201 || !createFile.has("id")) {
                    Files.deleteIfExists(filePath);
                    ApiResponse.send(exchange, createFile.toString(),
                            createFile.has("status") ? createFile.get("status").getAsInt() : 500);
                    return;
                }

                int fileId = createFile.get("id").getAsInt();
                JsonObject linkFile = UploadedFileDao.linkToStory(storyId, fileId);
                if (linkFile.get("status").getAsInt() != 200) {
                    UploadedFileDao.deleteByUser(fileId, userId);
                    Files.deleteIfExists(filePath);
                    ApiResponse.send(exchange, linkFile.toString(), linkFile.get("status").getAsInt());
                    return;
                }

                JsonObject response = new JsonObject();
                JsonObject file = new JsonObject();
                file.addProperty("id", fileId);
                file.addProperty("nombreArchivo", fileName.trim());
                file.addProperty("tipoArchivo", fileType);
                file.addProperty("rutaAlmacenamiento", relativePath);
                file.addProperty("tamanoBytes", fileBytes.length);
                response.add("archivo", file);
                response.addProperty("Mensaje", "Archivo fuente subido correctamente");
                response.addProperty("status", 201);
                ApiResponse.send(exchange, response.toString(), 201);

            } catch (IllegalArgumentException e) {
                ApiResponse.error(exchange, 400, "Archivo base64 inválido");
            } catch (Exception e) {
                ApiResponse.error(exchange, 500, "Error al subir archivo del relato: " + e.getMessage());
            }
        };
    }

    /**
     * Handler para eliminar archivo fuente de un relato.
     * Ruta: DELETE /api/v1/upload/relato?storyId=X&fileId=Y
     */
    public static HttpHandler eliminarArchivoRelato() {
        return exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Integer storyId = extractNumericQuery(query, "storyId");
            Integer fileId = extractNumericQuery(query, "fileId");
            int userId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));

            if (storyId == null || storyId <= 0 || fileId == null || fileId <= 0) {
                ApiResponse.error(exchange, 400, "Se requieren storyId y fileId válidos");
                return;
            }

            if (userId <= 0) {
                ApiResponse.error(exchange, 401, "Usuario no autenticado");
                return;
            }

            JsonObject accessValidation = StoryService.validateStoryOwnership(storyId, userId);
            if (accessValidation != null) {
                ApiResponse.send(exchange, accessValidation.toString(), accessValidation.get("status").getAsInt());
                return;
            }

            JsonObject storyFiles = UploadedFileDao.listByStory(storyId);
            if (!storyFiles.has("status") || storyFiles.get("status").getAsInt() != 200) {
                ApiResponse.send(exchange, storyFiles.toString(),
                        storyFiles.has("status") ? storyFiles.get("status").getAsInt() : 500);
                return;
            }

            JsonObject targetFile = null;
            JsonArray files = storyFiles.getAsJsonArray("archivos");
            for (JsonElement item : files) {
                if (!item.isJsonObject()) {
                    continue;
                }

                JsonObject file = item.getAsJsonObject();
                if (file.has("id") && file.get("id").getAsInt() == fileId) {
                    targetFile = file;
                    break;
                }
            }

            if (targetFile == null) {
                ApiResponse.error(exchange, 404, "Archivo no encontrado para este relato");
                return;
            }

            try {
                JsonObject deleteFile = UploadedFileDao.deleteByUser(fileId, userId);
                if (deleteFile.get("status").getAsInt() != 200) {
                    ApiResponse.send(exchange, deleteFile.toString(), deleteFile.get("status").getAsInt());
                    return;
                }

                if (targetFile.has("rutaAlmacenamiento")) {
                    Files.deleteIfExists(Paths.get(targetFile.get("rutaAlmacenamiento").getAsString()));
                }

                ApiResponse.send(exchange, deleteFile.toString(), 200);
            } catch (Exception e) {
                ApiResponse.error(exchange, 500, "Error al eliminar archivo del relato: " + e.getMessage());
            }
        };
    }

    private static Integer extractNumericQuery(String query, String key) {
        if (query == null || query.isBlank()) {
            return null;
        }

        String[] params = query.split("&");
        for (String param : params) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2 && parts[0].equals(key) && parts[1].matches("\\d+")) {
                return Integer.parseInt(parts[1]);
            }
        }

        return null;
    }

    private static String resolveStoryFileType(String fileName, String mimeType) {
        String lowerName = fileName.toLowerCase();
        String lowerMime = mimeType == null ? "" : mimeType.toLowerCase();

        if (lowerName.endsWith(".pdf") || lowerMime.equals("application/pdf")) {
            return "PDF";
        }

        if (lowerName.endsWith(".docx")
                || lowerMime.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return "DOCX";
        }

        if (lowerName.endsWith(".doc") || lowerMime.equals("application/msword")) {
            return "DOC";
        }

        return null;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }

        return fileName.substring(dotIndex).toLowerCase();
    }

    private static int getUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return -1;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        JsonObject tokenInfo = JwtUtil.validateToken(token);

        if (tokenInfo.has("Mensaje") || !tokenInfo.has("Id")) {
            return -1;
        }

        return tokenInfo.get("Id").getAsInt();
    }
}
