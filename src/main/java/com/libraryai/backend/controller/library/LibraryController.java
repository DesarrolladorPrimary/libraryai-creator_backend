package com.libraryai.backend.controller.library;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.library.LibraryService;
import com.libraryai.backend.util.JwtUtil;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador para documentos exportados visibles en biblioteca.
 */
public class LibraryController {

    /**
     * Lista los documentos exportados del usuario y permite filtrar por estantería.
     */
    public static HttpHandler listDocuments() {
        return exchange -> {
            int userId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
            if (userId <= 0) {
                ApiResponse.error(exchange, 401, "Usuario no autenticado");
                return;
            }

            Integer shelfId = extractNumericQuery(exchange.getRequestURI().getQuery(), "shelfId");
            JsonObject response = LibraryService.listDocuments(userId, shelfId);
            ApiResponse.send(exchange, response.toString(), response.get("status").getAsInt());
        };
    }

    /**
     * Elimina un documento exportado de la biblioteca del usuario.
     */
    public static HttpHandler deleteDocument() {
        return exchange -> {
            int userId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
            if (userId <= 0) {
                ApiResponse.error(exchange, 401, "Usuario no autenticado");
                return;
            }

            Integer fileId = extractNumericQuery(exchange.getRequestURI().getQuery(), "fileId");
            if (fileId == null || fileId <= 0) {
                ApiResponse.error(exchange, 400, "Se requiere fileId");
                return;
            }

            JsonObject response = LibraryService.deleteDocument(fileId, userId);
            ApiResponse.send(exchange, response.toString(), response.get("status").getAsInt());
        };
    }

    /**
     * Descarga un documento exportado validando ownership antes de leer disco.
     */
    public static HttpHandler downloadDocument() {
        return exchange -> {
            int userId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
            if (userId <= 0) {
                ApiResponse.error(exchange, 401, "Usuario no autenticado");
                return;
            }

            Integer fileId = extractNumericQuery(exchange.getRequestURI().getQuery(), "fileId");
            if (fileId == null || fileId <= 0) {
                ApiResponse.error(exchange, 400, "Se requiere fileId");
                return;
            }

            JsonObject response = LibraryService.getDocument(fileId, userId);
            if (!response.has("status") || response.get("status").getAsInt() != 200 || !response.has("archivo")) {
                ApiResponse.send(exchange, response.toString(), response.get("status").getAsInt());
                return;
            }

            JsonObject file = response.getAsJsonObject("archivo");
            Path path = Paths.get(file.get("rutaAlmacenamiento").getAsString());
            if (!Files.exists(path) || !Files.isReadable(path)) {
                ApiResponse.error(exchange, 404, "Documento no disponible");
                return;
            }

            byte[] bytes = Files.readAllBytes(path);
            String fileName = file.get("nombreArchivo").getAsString();
            String contentType = resolveContentType(fileName, file.get("tipoArchivo").getAsString());
            String disposition = "attachment; filename*=UTF-8''"
                    + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

            ApiResponse.sendBytes(exchange, bytes, 200, contentType, disposition);
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

    private static String resolveContentType(String fileName, String fileType) {
        String lowerName = String.valueOf(fileName).toLowerCase();
        String normalizedType = String.valueOf(fileType).toUpperCase();

        if (lowerName.endsWith(".pdf") || normalizedType.equals("PDF")) {
            return "application/pdf";
        }

        if (lowerName.endsWith(".docx") || normalizedType.equals("DOCX")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        return "application/msword";
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
