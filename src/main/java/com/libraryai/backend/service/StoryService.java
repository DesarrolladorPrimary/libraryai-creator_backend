package com.libraryai.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.libraryai.backend.dao.AIConfigurationDao;
import com.libraryai.backend.dao.SettingsDao;
import com.libraryai.backend.dao.StoryDao;
import com.libraryai.backend.dao.StoryVersionDao;
import com.libraryai.backend.dao.UploadedFileDao;
import com.libraryai.backend.dao.chats.ChatDao;
import com.libraryai.backend.models.Story;
import com.libraryai.backend.util.DocumentExportBuilder;

/**
 * Servicio para la lógica de negocio de relatos.
 */
public class StoryService {

    private static final String MODE_ARTIFICIAL = "Seccion_Artificial";
    private static final String MODE_CREATIVE = "Seccion_Creativa";
    private static final String DEFAULT_WRITING_STYLE = "Narrativo";
    private static final String DEFAULT_CREATIVITY_LEVEL = "Medio";
    private static final String DEFAULT_RESPONSE_LENGTH = "Media";
    private static final String DEFAULT_EMOTIONAL_TONE = "Neutral";
    private static final String EXPORT_UPLOAD_DIR = "uploads/exportados/";
    private static final String CREATIVE_MODERATION_MESSAGE =
            "La seccion creativa no admite contenido +18 o palabras bloqueadas. Ajusta el texto e intentalo de nuevo.";

    /**
     * Crea un nuevo relato con validaciones.
     * 
     * @param usuarioId ID del usuario creador
     * @param titulo Título del relato
     * @param modoOrigen Modo de origen ('Seccion_Artificial', 'Seccion_Creativa')
     * @param descripcion Descripción del relato
     * @param estanteriaId ID de la estantería (opcional)
     * @param modeloUsadoId ID del modelo de IA usado (opcional)
     * @return JsonObject con el resultado de la operación
     */
    public static JsonObject createStory(int usuarioId, String titulo, String modoOrigen, 
                                        String descripcion, Integer estanteriaId, Integer modeloUsadoId) {
        JsonObject basicValidation = validateStoryCreationInput(titulo, modoOrigen);
        if (basicValidation != null) {
            return basicValidation;
        }

        String normalizedTitle = titulo.trim();
        String normalizedDescription = descripcion != null ? descripcion.trim() : "";

        JsonObject moderationResult = validateCreativeDraftModeration(
                usuarioId,
                modoOrigen,
                normalizedTitle,
                normalizedDescription,
                "Creacion de borrador creativo bloqueada por moderacion");
        if (moderationResult != null) {
            return moderationResult;
        }

        JsonObject shelfValidation = validateShelfOwnership(
                usuarioId,
                estanteriaId,
                false,
                "Debes asignar una estantería existente antes de guardar el relato en biblioteca",
                "La estantería seleccionada no existe o no te pertenece");
        if (shelfValidation != null) {
            return shelfValidation;
        }

        // El modo artificial puede resolver modelo por plan aunque el cliente
        // no mande uno explícito; creativo solo lo usa si ya viene definido.
        JsonObject modelResolution = resolveModelIfNeeded(usuarioId, modoOrigen, modeloUsadoId, modeloUsadoId != null);
        if (isErrorResponse(modelResolution)) {
            return modelResolution;
        }
        Integer resolvedModelId = extractResolvedModelId(modelResolution, modeloUsadoId);
        
        // Crear el objeto Story
        LocalDateTime ahora = LocalDateTime.now();
        Story story = new Story(
            0, // ID se genera en la base de datos
            usuarioId,
            estanteriaId,
            resolvedModelId,
            normalizedTitle,
            modoOrigen,
            normalizedDescription,
            ahora,
            ahora
        );
        
        JsonObject createResult = StoryDao.create(story);
        if (!createResult.has("status") || createResult.get("status").getAsInt() != 201 || !createResult.has("id")) {
            return createResult;
        }

        JsonObject versionResult = StoryVersionDao.createVersion(
                createResult.get("id").getAsInt(),
                normalizedDescription,
                buildInitialVersionNote(modoOrigen),
                false);

        if (versionResult.has("status") && versionResult.get("status").getAsInt() == 201) {
            createResult.addProperty("version", versionResult.get("version").getAsInt());
            createResult.addProperty("Mensaje", "Relato creado y versionado correctamente");
            return createResult;
        }

        createResult.addProperty("Mensaje", "Relato creado, pero no se pudo guardar la versión inicial");
        createResult.add("detalleVersion", versionResult);
        return createResult;
    }

    /**
     * Obtiene todos los relatos de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return JsonObject con la lista de relatos
     */
    public static JsonObject getUserStories(int usuarioId) {
        if (usuarioId <= 0) {
            return buildErrorResponse("ID de usuario inválido", 400);
        }
        
        return StoryDao.findByUserId(usuarioId);
    }

    /**
     * Obtiene un relato específico por su ID.
     * 
     * @param relatoId ID del relato
     * @param usuarioId ID del usuario solicitante (para validación de permisos)
     * @return JsonObject con los datos del relato
     */
    public static JsonObject getStoryById(int relatoId, int usuarioId) {
        JsonObject invalidIds = validatePositiveIds(relatoId, usuarioId);
        if (invalidIds != null) {
            return invalidIds;
        }

        JsonObject result = findOwnedStory(relatoId, usuarioId, "No tienes permisos para acceder a este relato");
        if (!hasOkStatus(result)) {
            return result;
        }

        JsonObject configResult = AIConfigurationDao.findByStoryId(relatoId);
        if (hasOkStatus(configResult) && configResult.has("configuracionIA")) {
            result.add("configuracionIA", configResult.getAsJsonObject("configuracionIA"));
        } else {
            result.add("configuracionIA", buildDefaultAIConfig(relatoId));
        }

        result.add("archivosFuente", extractFilesByOrigin(relatoId, "Subido"));
        result.add("archivosExportados", extractFilesByOrigin(relatoId, "Exportado"));
        
        return result;
    }

    /**
     * Actualiza un relato existente.
     * 
     * @param relatoId ID del relato a actualizar
     * @param usuarioId ID del usuario solicitante
     * @param titulo Nuevo título (opcional)
     * @param modoOrigen Nuevo modo de origen (opcional)
     * @param descripcion Nueva descripción (opcional)
     * @param estanteriaId Nueva estantería (opcional)
     * @param modeloUsadoId Nuevo modelo usado (opcional)
     * @return JsonObject con el resultado de la operación
     */
    public static JsonObject updateStory(int relatoId, int usuarioId, String titulo, 
                                        String modoOrigen, String descripcion, 
                                        Integer estanteriaId, Integer modeloUsadoId,
                                        boolean updateShelfAssignment) {
        JsonObject invalidIds = validatePositiveIds(relatoId, usuarioId);
        if (invalidIds != null) {
            return invalidIds;
        }

        JsonObject existingStory = findOwnedStory(relatoId, usuarioId, "No tienes permisos para modificar este relato");
        if (!hasOkStatus(existingStory)) {
            return existingStory;
        }
        JsonObject relatoData = existingStory.getAsJsonObject("relato");
        
        // Validar modo de origen si se proporciona
        if (modoOrigen != null && !modoOrigen.trim().isEmpty() && !isValidOriginMode(modoOrigen)) {
            return buildErrorResponse(
                    "Modo de origen inválido. Debe ser 'Seccion_Artificial' o 'Seccion_Creativa'",
                    400);
        }
        
        // Validar título si se proporciona
        if (titulo != null && titulo.length() > 255) {
            return buildErrorResponse("El título no puede exceder 255 caracteres", 400);
        }

        String currentTitle = relatoData.get("titulo").getAsString();
        String currentDescription = relatoData.get("descripcion").getAsString();
        String nextOriginMode = modoOrigen != null ? modoOrigen.trim() : relatoData.get("modoOrigen").getAsString();
        Integer currentShelfId = getOptionalInt(relatoData, "estanteriaId");
        Integer currentModelId = getOptionalInt(relatoData, "modeloUsadoId");
        Integer nextShelfId = updateShelfAssignment ? estanteriaId : currentShelfId;
        Integer nextModelId = modeloUsadoId != null ? modeloUsadoId : currentModelId;

        // Reutiliza la misma resolución de modelo del create para no divergir
        // entre borradores nuevos y relatos ya existentes.
        JsonObject modelResolution = resolveModelIfNeeded(usuarioId, nextOriginMode, nextModelId, modeloUsadoId != null);
        if (isErrorResponse(modelResolution)) {
            return modelResolution;
        }
        nextModelId = extractResolvedModelId(modelResolution, nextModelId);
        
        String nextTitle = titulo != null ? titulo.trim() : currentTitle;
        String nextDescription = descripcion != null ? descripcion.trim() : currentDescription;
        boolean shouldCreateDraftVersion = descripcion != null && !nextDescription.equals(currentDescription);

        JsonObject moderationResult = validateCreativeDraftModeration(
                usuarioId,
                nextOriginMode,
                nextTitle,
                nextDescription,
                "Actualizacion de borrador creativo bloqueada por moderacion");
        if (moderationResult != null) {
            return moderationResult;
        }

        JsonObject shelfValidation = validateShelfOwnership(
                usuarioId,
                nextShelfId,
                false,
                "Debes asignar una estantería existente antes de guardar el relato en biblioteca",
                "La estantería seleccionada no existe o no te pertenece");
        if (shelfValidation != null) {
            return shelfValidation;
        }

        // Crear objeto Story con los datos actualizados
        Story story = new Story(
            relatoId,
            usuarioId,
            nextShelfId,
            nextModelId,
            nextTitle,
            nextOriginMode,
            nextDescription,
            parseStoryCreatedAt(relatoData),
            LocalDateTime.now() // Actualizar fecha de modificación
        );

        JsonObject updateResult = StoryDao.update(story);
        if (!updateResult.has("status") || updateResult.get("status").getAsInt() != 200) {
            return updateResult;
        }

        if (!shouldCreateDraftVersion) {
            return updateResult;
        }

        JsonObject versionResult = StoryVersionDao.createVersion(
                relatoId,
                nextDescription,
                buildDraftVersionNote(nextOriginMode),
                false);

        if (versionResult.has("status") && versionResult.get("status").getAsInt() == 201) {
            updateResult.addProperty("version", versionResult.get("version").getAsInt());
            updateResult.addProperty("Mensaje", "Relato actualizado y versionado correctamente");
            return updateResult;
        }

        updateResult.addProperty("Mensaje", "Relato actualizado, pero no se pudo guardar la versión");
        updateResult.add("detalleVersion", versionResult);
        return updateResult;
    }

    private static JsonObject validateCreativeDraftModeration(
            int userId,
            String originMode,
            String title,
            String description,
            String logReason) {
        // Solo moderamos el flujo creativo manual; Poly ya modera sus entradas
        // y salidas en su propio servicio.
        if (!MODE_CREATIVE.equals(originMode)) {
            return null;
        }

        StringBuilder content = new StringBuilder();
        if (title != null && !title.isBlank()) {
            content.append(title.trim());
        }

        if (description != null && !description.isBlank()) {
            if (content.length() > 0) {
                content.append("\n\n");
            }
            content.append(description.trim());
        }

        return ModerationService.validateText(
                content.toString(),
                userId,
                CREATIVE_MODERATION_MESSAGE,
                logReason);
    }

    private static JsonObject validateShelfOwnership(
            int userId,
            Integer shelfId,
            boolean required,
            String requiredMessage,
            String invalidMessage) {
        if (shelfId == null || shelfId <= 0) {
            if (!required) {
                return null;
            }

            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", requiredMessage);
            response.addProperty("status", 400);
            return response;
        }

        JsonArray shelves = ShelfService.obtenerEstanterias(userId);
        for (JsonElement element : shelves) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject shelf = element.getAsJsonObject();
            if (shelf.has("status") && shelf.get("status").getAsInt() >= 400) {
                return shelf;
            }

            if (shelf.has("id") && shelf.get("id").getAsInt() == shelfId) {
                return null;
            }
        }

        JsonObject response = new JsonObject();
        response.addProperty("Mensaje", invalidMessage);
        response.addProperty("status", 400);
        return response;
    }

    /**
     * Elimina un relato existente.
     * 
     * @param relatoId ID del relato a eliminar
     * @param usuarioId ID del usuario solicitante
     * @return JsonObject con el resultado de la operación
     */
    public static JsonObject deleteStory(int relatoId, int usuarioId) {
        JsonObject invalidIds = validatePositiveIds(relatoId, usuarioId);
        if (invalidIds != null) {
            return invalidIds;
        }

        JsonObject existingStory = findOwnedStory(relatoId, usuarioId, "No tienes permisos para eliminar este relato");
        if (!hasOkStatus(existingStory)) {
            return existingStory;
        }
        
        JsonObject cleanupFiles = deleteStoryFiles(relatoId, usuarioId);
        if (cleanupFiles != null) {
            return cleanupFiles;
        }

        JsonObject deleteMessages = ChatDao.deleteByStory(relatoId);
        if (deleteMessages.has("status") && deleteMessages.get("status").getAsInt() != 200) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", deleteMessages.get("Mensaje").getAsString());
            response.addProperty("status", deleteMessages.get("status").getAsInt());
            return response;
        }

        JsonObject deleteConfig = AIConfigurationDao.deleteByStory(relatoId);
        if (isErrorResponse(deleteConfig)) {
            return deleteConfig;
        }

        JsonObject deleteVersions = StoryVersionDao.deleteByStory(relatoId);
        if (isErrorResponse(deleteVersions)) {
            return deleteVersions;
        }

        return StoryDao.delete(relatoId);
    }

    public static JsonObject getAIConfiguration(int relatoId, int usuarioId) {
        JsonObject accessValidation = validateStoryOwnership(relatoId, usuarioId);
        if (accessValidation != null) {
            return accessValidation;
        }

        JsonObject result = AIConfigurationDao.findByStoryId(relatoId);
        if (result.has("status") && result.get("status").getAsInt() == 200) {
            return result;
        }

        JsonObject response = new JsonObject();
        response.add("configuracionIA", buildDefaultAIConfig(relatoId));
        response.addProperty("status", 200);
        return response;
    }

    public static JsonObject updateAIConfiguration(int relatoId, int usuarioId, String writingStyle,
            String creativityLevel, String responseLength, String emotionalTone) {
        JsonObject accessValidation = validateStoryOwnership(relatoId, usuarioId);
        if (accessValidation != null) {
            return accessValidation;
        }

        String nextWritingStyle = normalizeOrDefault(writingStyle, DEFAULT_WRITING_STYLE);
        String nextCreativityLevel = normalizeOrDefault(creativityLevel, DEFAULT_CREATIVITY_LEVEL);
        String nextResponseLength = normalizeOrDefault(responseLength, DEFAULT_RESPONSE_LENGTH);
        String nextEmotionalTone = normalizeOrDefault(emotionalTone, DEFAULT_EMOTIONAL_TONE);

        if (!nextCreativityLevel.equals("Bajo") && !nextCreativityLevel.equals("Medio")
                && !nextCreativityLevel.equals("Alto") && !nextCreativityLevel.equals("Extremo")) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Nivel de creatividad inválido");
            response.addProperty("status", 400);
            return response;
        }

        if (!nextResponseLength.equals("Corta") && !nextResponseLength.equals("Media")
                && !nextResponseLength.equals("Larga")) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Longitud de respuesta inválida");
            response.addProperty("status", 400);
            return response;
        }

        JsonObject result = AIConfigurationDao.upsert(relatoId, nextWritingStyle, nextCreativityLevel,
                nextResponseLength, nextEmotionalTone);

        if (result.has("status") && result.get("status").getAsInt() == 200) {
            result.add("configuracionIA", buildAIConfig(relatoId, nextWritingStyle, nextCreativityLevel,
                    nextResponseLength, nextEmotionalTone));
        }

        return result;
    }

    public static JsonObject exportStory(int relatoId, int usuarioId, String titulo, String contenido, String formato,
            Integer estanteriaId, boolean updateShelfAssignment, String exportFileName, String exportFileType,
            String exportFileBase64) {
        JsonObject accessValidation = validateStoryOwnership(relatoId, usuarioId);
        if (accessValidation != null) {
            return accessValidation;
        }

        String normalizedTitle = titulo == null ? "" : titulo.trim();
        String normalizedContent = contenido == null ? "" : contenido.trim();
        String normalizedFormat = formato == null ? "" : formato.trim();

        if (normalizedTitle.isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El título del libro es obligatorio");
            response.addProperty("status", 400);
            return response;
        }

        if (normalizedContent.isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "No hay contenido para exportar");
            response.addProperty("status", 400);
            return response;
        }

        if (!normalizedFormat.equalsIgnoreCase("word") && !normalizedFormat.equalsIgnoreCase("pdf")) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Formato de exportación inválido");
            response.addProperty("status", 400);
            return response;
        }

        JsonObject exportDescriptor = resolveExportDescriptor(normalizedTitle, normalizedFormat, exportFileName,
                exportFileType, exportFileBase64);
        if (isErrorResponse(exportDescriptor)) {
            return exportDescriptor;
        }

        JsonObject duplicateValidation = validateExportFileNameAvailable(
                usuarioId,
                relatoId,
                exportDescriptor.get("nombreArchivo").getAsString());
        if (duplicateValidation != null) {
            return duplicateValidation;
        }

        JsonObject moderationResult = ModerationService.validateText(
                normalizedContent,
                usuarioId,
                "No puedes exportar contenido con palabras indebidas o no aptas",
                "Intento de exportacion bloqueada");
        if (moderationResult != null) {
            return moderationResult;
        }

        JsonObject currentStory = StoryDao.findById(relatoId);
        if (currentStory.get("status").getAsInt() != 200) {
            return currentStory;
        }

        JsonObject storyData = currentStory.getAsJsonObject("relato");
        String originMode = storyData.get("modoOrigen").getAsString();

        Integer currentShelfId = getOptionalInt(storyData, "estanteriaId");
        Integer nextShelfId = updateShelfAssignment ? estanteriaId : currentShelfId;

        JsonObject shelfValidation = validateShelfOwnership(
                usuarioId,
                nextShelfId,
                true,
                "Debes asignar una estantería antes de convertir el relato en libro",
                "La estantería seleccionada no existe o no te pertenece");
        if (shelfValidation != null) {
            return shelfValidation;
        }

        Story story = new Story(
                relatoId,
                usuarioId,
                nextShelfId,
                getOptionalInt(storyData, "modeloUsadoId"),
                normalizedTitle,
                originMode,
                normalizedContent,
                parseStoryCreatedAt(storyData),
                LocalDateTime.now());

        JsonObject updateStory = StoryDao.update(story);
        if (updateStory.get("status").getAsInt() != 200) {
            return updateStory;
        }

        JsonObject createVersion = StoryVersionDao.createVersion(
                relatoId,
                normalizedContent,
                "Exportación en formato " + normalizedFormat.toUpperCase(),
                true);

        JsonObject response = new JsonObject();
        response.addProperty("Mensaje", "Libro generado y versionado correctamente");
        response.addProperty("status", createVersion.get("status").getAsInt() == 201 ? 200 : createVersion.get("status").getAsInt());
        if (createVersion.has("version")) {
            response.addProperty("version", createVersion.get("version").getAsInt());
        }

        int previousExportCount = countExportedFiles(relatoId);

        JsonObject exportFileResult = persistExportedFile(relatoId, usuarioId, normalizedTitle, normalizedContent,
                normalizedFormat, exportFileName, exportFileType, exportFileBase64);
        if (exportFileResult != null) {
            if (exportFileResult.get("status").getAsInt() == 201) {
                int exportedFileId = exportFileResult.get("id").getAsInt();
                response.addProperty("archivoExportadoId", exportedFileId);

                JsonObject cleanupOldExports = removePreviousExportedFiles(relatoId, usuarioId, exportedFileId);
                if (cleanupOldExports != null && cleanupOldExports.has("status")
                        && cleanupOldExports.get("status").getAsInt() != 200) {
                    response.addProperty("Mensaje", cleanupOldExports.get("Mensaje").getAsString());
                    response.addProperty("status", cleanupOldExports.get("status").getAsInt());
                    response.add("detalleArchivo", cleanupOldExports);
                    return response;
                }

                boolean replacedExistingExport = previousExportCount > 0;
                response.addProperty("archivoSobrescrito", replacedExistingExport);
                response.addProperty("Mensaje", replacedExistingExport
                        ? "Libro generado, versionado y archivo actualizado correctamente"
                        : "Libro generado, versionado y archivo registrado correctamente");

                if (cleanupOldExports != null && cleanupOldExports.has("detalleArchivos")) {
                    response.addProperty("detalleArchivos",
                            cleanupOldExports.get("detalleArchivos").getAsString());
                }
            } else {
                response.addProperty("Mensaje",
                        exportFileResult.has("Mensaje")
                                ? exportFileResult.get("Mensaje").getAsString()
                                : "No se pudo registrar el archivo exportado");
                response.addProperty("status", exportFileResult.get("status").getAsInt());
                response.add("detalleArchivo", exportFileResult);
            }
        }

        return response;
    }

    /**
     * Obtiene estadísticas de relatos de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return JsonObject con estadísticas
     */
    public static JsonObject getUserStoryStats(int usuarioId) {
        if (usuarioId <= 0) {
            return buildErrorResponse("ID de usuario inválido", 400);
        }

        JsonObject response = new JsonObject();
        
        try {
            int totalRelatos = StoryDao.countByUserId(usuarioId);
            
            JsonObject stats = new JsonObject();
            stats.addProperty("totalRelatos", totalRelatos);
            stats.addProperty("usuarioId", usuarioId);
            
            // TODO: Agregar más estadísticas cuando tengamos más datos
            // - Relatos por modo de origen
            // - Relatos por estantería
            // - Último relato creado
            
            response.add("estadisticas", stats);
            response.addProperty("status", 200);
            
        } catch (Exception e) {
            response.addProperty("Mensaje", "Error al obtener estadísticas: " + e.getMessage());
            response.addProperty("status", 500);
        }
        
        return response;
    }

    public static JsonObject validateStoryOwnership(int relatoId, int usuarioId) {
        JsonObject result = findOwnedStory(relatoId, usuarioId, "No tienes permisos para acceder a este relato");
        return hasOkStatus(result) ? null : result;
    }

    private static String buildDraftVersionNote(String originMode) {
        if (MODE_ARTIFICIAL.equals(originMode)) {
            return "Guardado de borrador desde Poly";
        }

        if (MODE_CREATIVE.equals(originMode)) {
            return "Guardado de borrador desde Creativo";
        }

        return "Guardado de borrador";
    }

    private static String buildInitialVersionNote(String originMode) {
        if (MODE_ARTIFICIAL.equals(originMode)) {
            return "Creación inicial del relato desde Poly";
        }

        if (MODE_CREATIVE.equals(originMode)) {
            return "Creación inicial del relato desde Creativo";
        }

        return "Creación inicial del relato";
    }

    private static JsonObject buildDefaultAIConfig(int storyId) {
        return buildAIConfig(storyId, DEFAULT_WRITING_STYLE, DEFAULT_CREATIVITY_LEVEL,
                DEFAULT_RESPONSE_LENGTH, DEFAULT_EMOTIONAL_TONE);
    }

    private static JsonObject buildAIConfig(int storyId, String writingStyle, String creativityLevel,
            String responseLength, String emotionalTone) {
        JsonObject config = new JsonObject();
        config.addProperty("relatoId", storyId);
        config.addProperty("estiloEscritura", writingStyle);
        config.addProperty("nivelCreatividad", creativityLevel);
        config.addProperty("longitudRespuesta", responseLength);
        config.addProperty("tonoEmocional", emotionalTone);
        return config;
    }

    private static String normalizeOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }

    private static JsonObject persistExportedFile(int storyId, int userId, String title, String content, String format,
            String fileName, String fileType, String fileBase64) {
        String normalizedFormat = format == null ? "" : format.trim().toLowerCase();
        if (!normalizedFormat.equals("word") && !normalizedFormat.equals("pdf")) {
            return null;
        }

        try {
            JsonObject exportDescriptor = resolveExportDescriptor(title, normalizedFormat, fileName, fileType,
                    fileBase64);
            if (isErrorResponse(exportDescriptor)) {
                return exportDescriptor;
            }

            String normalizedType = exportDescriptor.get("tipoArchivo").getAsString();
            String sanitizedName = exportDescriptor.get("nombreArchivo").getAsString();
            boolean hasClientFile = exportDescriptor.get("usaArchivoCliente").getAsBoolean();
            byte[] fileBytes = hasClientFile
                    ? decodeExportedFileBase64(fileBase64)
                    : normalizedType.equals("PDF")
                            ? DocumentExportBuilder.buildPdfDocument(title, content)
                            : DocumentExportBuilder.buildWordDocument(title, content);

            if (fileBytes.length <= 0) {
                JsonObject response = new JsonObject();
                response.addProperty("Mensaje", "Archivo exportado inválido");
                response.addProperty("status", 400);
                return response;
            }

            JsonObject duplicateValidation = validateExportFileNameAvailable(userId, storyId, sanitizedName);
            if (duplicateValidation != null) {
                return duplicateValidation;
            }

            // La cuota se valida justo antes de persistir para contar el peso
            // real del archivo final y no una estimación previa.
            JsonObject quotaValidation = validateStorageQuota(userId, fileBytes.length,
                    "guardar documentos en la biblioteca");
            if (quotaValidation != null) {
                return quotaValidation;
            }

            Path exportPath = Paths.get(EXPORT_UPLOAD_DIR);
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }

            String extension = sanitizedName.substring(sanitizedName.lastIndexOf(".")).toLowerCase();
            String storedName = "export_" + storyId + "_" + UUID.randomUUID() + extension;
            Path filePath = exportPath.resolve(storedName);
            Files.write(filePath, fileBytes);

            String relativePath = EXPORT_UPLOAD_DIR + storedName;
            JsonObject createdFile = UploadedFileDao.create(userId, sanitizedName, normalizedType, "Exportado",
                    relativePath, fileBytes.length);

            if (!createdFile.has("status") || createdFile.get("status").getAsInt() != 201 || !createdFile.has("id")) {
                Files.deleteIfExists(filePath);
                return createdFile;
            }

            // Si falla el enlace con el relato, se revierte también el archivo
            // físico para no dejar basura en disco ni registros huérfanos.
            JsonObject linkFile = UploadedFileDao.linkToStory(storyId, createdFile.get("id").getAsInt());
            if (!linkFile.has("status") || linkFile.get("status").getAsInt() != 200) {
                UploadedFileDao.deleteByUser(createdFile.get("id").getAsInt(), userId);
                Files.deleteIfExists(filePath);
                return linkFile;
            }

            return createdFile;
        } catch (IllegalArgumentException e) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Archivo exportado base64 inválido");
            response.addProperty("status", 400);
            return response;
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Error al registrar archivo exportado: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    private static JsonObject resolveExportDescriptor(String title, String normalizedFormat, String fileName,
            String fileType, String fileBase64) {
        JsonObject descriptor = new JsonObject();

        boolean hasClientFile = fileName != null && !fileName.trim().isEmpty()
                && fileType != null && !fileType.trim().isEmpty()
                && fileBase64 != null && !fileBase64.trim().isEmpty();

        String normalizedType;
        String sanitizedName;

        if (hasClientFile) {
            normalizedType = fileType.trim().toUpperCase();
            if (!normalizedType.equals("DOC") && !normalizedType.equals("DOCX") && !normalizedType.equals("PDF")) {
                return buildErrorResponse("Tipo de archivo exportado inválido", 400);
            }

            sanitizedName = sanitizeExportFileName(fileName, normalizedType);
        } else {
            normalizedType = normalizedFormat.equals("pdf") ? "PDF" : "DOC";
            sanitizedName = sanitizeExportFileName(title, normalizedType);
        }

        descriptor.addProperty("tipoArchivo", normalizedType);
        descriptor.addProperty("nombreArchivo", sanitizedName);
        descriptor.addProperty("usaArchivoCliente", hasClientFile);
        return descriptor;
    }

    private static byte[] decodeExportedFileBase64(String fileBase64) {
        String rawBase64 = fileBase64.contains(",") ? fileBase64.split(",")[1] : fileBase64;
        return Base64.getDecoder().decode(rawBase64);
    }

    private static JsonObject validateExportFileNameAvailable(int userId, int storyId, String fileName) {
        if (!UploadedFileDao.existsByUserAndOriginAndNameExcludingStory(userId, "Exportado", fileName, storyId)) {
            return null;
        }

        JsonObject response = new JsonObject();
        response.addProperty("Mensaje", "Ya tienes un libro con ese nombre en la biblioteca");
        response.addProperty("status", 409);
        response.addProperty("nombreArchivo", fileName);
        return response;
    }

    private static String sanitizeExportFileName(String fileName, String normalizedType) {
        String sanitizedName = String.valueOf(fileName == null ? "" : fileName).trim()
                .replaceAll("[\\\\/:*?\"<>|]+", "-")
                .replaceAll("\\s+", " ");

        if (sanitizedName.isBlank()) {
            sanitizedName = "historia-exportada";
        }

        String expectedExtension = switch (normalizedType) {
            case "PDF" -> ".pdf";
            case "DOCX" -> ".docx";
            default -> ".doc";
        };

        if (!sanitizedName.toLowerCase().endsWith(expectedExtension)) {
            int dotIndex = sanitizedName.lastIndexOf('.');
            if (dotIndex > 0) {
                sanitizedName = sanitizedName.substring(0, dotIndex);
            }
            sanitizedName = sanitizedName + expectedExtension;
        }

        return sanitizedName;
    }

    private static int countExportedFiles(int storyId) {
        JsonObject filesResult = UploadedFileDao.listByStoryAndOrigin(storyId, "Exportado");
        if (!hasOkStatus(filesResult) || !filesResult.has("archivos")) {
            return 0;
        }

        return filesResult.getAsJsonArray("archivos").size();
    }

    private static JsonObject removePreviousExportedFiles(int storyId, int userId, int keepFileId) {
        JsonObject filesResult = UploadedFileDao.listByStoryAndOrigin(storyId, "Exportado");
        if (!hasOkStatus(filesResult) || !filesResult.has("archivos")) {
            return filesResult;
        }

        int failedPhysicalDeletes = 0;

        for (JsonElement element : filesResult.getAsJsonArray("archivos")) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject file = element.getAsJsonObject();
            int fileId = file.get("id").getAsInt();
            if (fileId == keepFileId) {
                continue;
            }

            JsonObject unlinkFile = UploadedFileDao.unlinkFromStory(storyId, fileId);
            if (!hasOkStatus(unlinkFile)) {
                return unlinkFile;
            }

            if (UploadedFileDao.countStoryLinks(fileId) > 0) {
                continue;
            }

            JsonObject deleteFile = UploadedFileDao.deleteByUser(fileId, userId);
            if (!hasOkStatus(deleteFile)) {
                return deleteFile;
            }

            String filePath = file.has("rutaAlmacenamiento") && !file.get("rutaAlmacenamiento").isJsonNull()
                    ? file.get("rutaAlmacenamiento").getAsString()
                    : "";
            if (!filePath.isBlank()) {
                try {
                    Files.deleteIfExists(Path.of(filePath));
                } catch (Exception e) {
                    failedPhysicalDeletes++;
                }
            }
        }

        JsonObject response = new JsonObject();
        response.addProperty("status", 200);
        if (failedPhysicalDeletes > 0) {
            response.addProperty("detalleArchivos",
                    "Se actualizó el libro, pero " + failedPhysicalDeletes
                            + " archivo(s) exportado(s) antiguos no pudieron borrarse del disco");
        }
        return response;
    }

    private static JsonArray extractFilesByOrigin(int storyId, String origin) {
        JsonObject filesResult = UploadedFileDao.listByStoryAndOrigin(storyId, origin);
        if (filesResult.has("status") && filesResult.get("status").getAsInt() == 200
                && filesResult.has("archivos")) {
            return filesResult.getAsJsonArray("archivos");
        }

        return new JsonArray();
    }

    private static JsonObject deleteStoryFiles(int storyId, int userId) {
        JsonObject filesResult = UploadedFileDao.listByStory(storyId);
        if (!filesResult.has("status") || filesResult.get("status").getAsInt() != 200 || !filesResult.has("archivos")) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "No se pudieron obtener los archivos del relato");
            response.addProperty("status", 500);
            return response;
        }

        for (JsonElement element : filesResult.getAsJsonArray("archivos")) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject file = element.getAsJsonObject();
            int fileId = file.get("id").getAsInt();

            JsonObject unlinkFile = UploadedFileDao.unlinkFromStory(storyId, fileId);
            if (!unlinkFile.has("status") || unlinkFile.get("status").getAsInt() != 200) {
                return unlinkFile;
            }

            if (UploadedFileDao.countStoryLinks(fileId) > 0) {
                continue;
            }

            JsonObject deleteFile = UploadedFileDao.deleteByUser(fileId, userId);
            if (!deleteFile.has("status") || deleteFile.get("status").getAsInt() != 200) {
                return deleteFile;
            }

            try {
                if (file.has("rutaAlmacenamiento")) {
                    Files.deleteIfExists(Paths.get(file.get("rutaAlmacenamiento").getAsString()));
                }
            } catch (Exception e) {
                JsonObject response = new JsonObject();
                response.addProperty("Mensaje", "Archivo eliminado de la base de datos, pero no del disco");
                response.addProperty("status", 500);
                return response;
            }
        }

        return null;
    }

    public static JsonObject validateStorageQuota(int userId, long newFileBytes, String actionDescription) {
        JsonObject subscription = SettingsDao.getSuscripcionActiva(userId);
        if (!hasOkStatus(subscription)) {
            return buildErrorResponse("No fue posible validar el almacenamiento disponible", 500);
        }

        boolean unlimitedStorage = subscription.has("almacenamientoIlimitado")
                && subscription.get("almacenamientoIlimitado").getAsBoolean();
        if (unlimitedStorage) {
            return null;
        }

        double storageLimitMb = subscription.has("limiteAlmacenamientoMb")
                ? subscription.get("limiteAlmacenamientoMb").getAsDouble()
                : 500d;
        if (storageLimitMb <= 0) {
            storageLimitMb = 500d;
        }

        long usedBytes = UploadedFileDao.sumBytesByUser(userId);
        double nextUsageMb = (usedBytes + newFileBytes) / 1024d / 1024d;
        if (nextUsageMb <= storageLimitMb) {
            return null;
        }

        JsonObject response = new JsonObject();
        String normalizedAction = actionDescription == null || actionDescription.isBlank()
                ? "guardar archivos en tu cuenta"
                : actionDescription.trim();
        response.addProperty("Mensaje", "Has superado la cuota disponible para " + normalizedAction);
        response.addProperty("status", 409);
        response.addProperty("almacenamientoUsadoMb", Math.round(nextUsageMb * 100.0d) / 100.0d);
        response.addProperty("limiteAlmacenamientoMb", storageLimitMb);
        return response;
    }

    private static JsonObject validateStoryCreationInput(String titulo, String modoOrigen) {
        if (titulo == null || titulo.trim().isEmpty()) {
            return buildErrorResponse("El título es obligatorio", 400);
        }

        if (modoOrigen == null || modoOrigen.trim().isEmpty()) {
            return buildErrorResponse("El modo de origen es obligatorio", 400);
        }

        if (!isValidOriginMode(modoOrigen)) {
            return buildErrorResponse(
                    "Modo de origen inválido. Debe ser 'Seccion_Artificial' o 'Seccion_Creativa'",
                    400);
        }

        if (titulo.length() > 255) {
            return buildErrorResponse("El título no puede exceder 255 caracteres", 400);
        }

        return null;
    }

    private static JsonObject findOwnedStory(int relatoId, int usuarioId, String forbiddenMessage) {
        JsonObject invalidIds = validatePositiveIds(relatoId, usuarioId);
        if (invalidIds != null) {
            return invalidIds;
        }

        JsonObject existingStory = StoryDao.findById(relatoId);
        if (!hasOkStatus(existingStory)) {
            return buildErrorResponse("El relato no existe", 404);
        }

        JsonObject storyData = existingStory.getAsJsonObject("relato");
        if (storyData.get("usuarioId").getAsInt() != usuarioId) {
            return buildErrorResponse(forbiddenMessage, 403);
        }

        return existingStory;
    }

    private static JsonObject validatePositiveIds(int relatoId, int usuarioId) {
        if (relatoId <= 0 || usuarioId <= 0) {
            return buildErrorResponse("IDs inválidos", 400);
        }

        return null;
    }

    private static JsonObject resolveModelIfNeeded(int usuarioId, String originMode, Integer modelId,
            boolean failIfUnavailable) {
        if (!MODE_ARTIFICIAL.equals(originMode) && modelId == null) {
            return null;
        }

        return SettingsService.resolveModeloIA(usuarioId, modelId, failIfUnavailable);
    }

    private static Integer extractResolvedModelId(JsonObject modelResolution, Integer fallbackModelId) {
        if (modelResolution == null || !hasOkStatus(modelResolution) || !modelResolution.has("modelo")) {
            return fallbackModelId;
        }

        JsonObject resolvedModel = modelResolution.getAsJsonObject("modelo");
        return resolvedModel.has("id") ? resolvedModel.get("id").getAsInt() : fallbackModelId;
    }

    private static Integer getOptionalInt(JsonObject source, String field) {
        return source.has(field) ? source.get(field).getAsInt() : null;
    }

    private static LocalDateTime parseStoryCreatedAt(JsonObject storyData) {
        return LocalDateTime.parse(storyData.get("fechaCreacion").getAsString().replace(" ", "T"));
    }

    private static boolean isValidOriginMode(String originMode) {
        return MODE_ARTIFICIAL.equals(originMode) || MODE_CREATIVE.equals(originMode);
    }

    private static boolean hasOkStatus(JsonObject response) {
        return response != null && response.has("status") && response.get("status").getAsInt() == 200;
    }

    private static boolean isErrorResponse(JsonObject response) {
        return response != null && response.has("status") && response.get("status").getAsInt() != 200;
    }

    private static JsonObject buildErrorResponse(String message, int status) {
        JsonObject response = new JsonObject();
        response.addProperty("Mensaje", message);
        response.addProperty("status", status);
        return response;
    }
}
