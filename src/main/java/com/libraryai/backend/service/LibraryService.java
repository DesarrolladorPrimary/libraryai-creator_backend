package com.libraryai.backend.service;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.UploadedFileDao;

/**
 * Lógica de negocio de documentos exportados visibles en biblioteca.
 */
public class LibraryService {

    public static JsonObject listDocuments(int userId, Integer shelfId) {
        if (userId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Usuario inválido");
            response.addProperty("status", 400);
            return response;
        }

        return UploadedFileDao.listExportedByUser(userId, shelfId);
    }

    public static JsonObject getDocument(int fileId, int userId) {
        if (fileId <= 0 || userId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "IDs inválidos");
            response.addProperty("status", 400);
            return response;
        }

        return UploadedFileDao.findByIdAndUserAndOrigin(fileId, userId, "Exportado");
    }

    public static JsonObject deleteDocument(int fileId, int userId) {
        JsonObject targetFile = getDocument(fileId, userId);
        if (!targetFile.has("status") || targetFile.get("status").getAsInt() != 200 || !targetFile.has("archivo")) {
            return targetFile;
        }

        JsonObject file = targetFile.getAsJsonObject("archivo");
        JsonObject deleteFile = UploadedFileDao.deleteByUser(fileId, userId);
        if (!deleteFile.has("status") || deleteFile.get("status").getAsInt() != 200) {
            return deleteFile;
        }

        try {
            if (file.has("rutaAlmacenamiento")) {
                Files.deleteIfExists(Paths.get(file.get("rutaAlmacenamiento").getAsString()));
            }
        } catch (Exception e) {
            deleteFile.addProperty("Mensaje", "Documento eliminado de la base de datos, pero no del disco");
        }

        return deleteFile;
    }

    public static JsonArray extractDocuments(JsonObject response) {
        if (response.has("documentos") && response.get("documentos").isJsonArray()) {
            return response.getAsJsonArray("documentos");
        }

        return new JsonArray();
    }

    public static JsonObject findDocumentInArray(JsonArray documents, int fileId) {
        for (JsonElement item : documents) {
            if (item.isJsonObject()) {
                JsonObject document = item.getAsJsonObject();
                if (document.has("id") && document.get("id").getAsInt() == fileId) {
                    return document;
                }
            }
        }

        return null;
    }
}
