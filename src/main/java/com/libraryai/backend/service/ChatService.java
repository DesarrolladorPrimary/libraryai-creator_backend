package com.libraryai.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.libraryai.backend.dao.AIConfigurationDao;
import com.libraryai.backend.dao.SettingsDao;
import com.libraryai.backend.dao.StoryDao;
import com.libraryai.backend.dao.UploadedFileDao;
import com.libraryai.backend.dao.chats.ChatDao;
import com.libraryai.backend.service.ai.GeminiService;
import com.libraryai.backend.util.DocumentTextExtractor;

/**
 * Servicio para la lógica de negocio de chat.
 */
public class ChatService {

    private static final int MAX_SOURCE_FILES_IN_PROMPT = 2;
    private static final String CANVAS_UPDATE_MESSAGE = "He actualizado el canvas con un nuevo borrador final.";

    private static final class PolyResponsePayload {
        private final String chatMessage;
        private final String canvasDraft;

        private PolyResponsePayload(String chatMessage, String canvasDraft) {
            this.chatMessage = chatMessage == null ? "" : chatMessage.trim();
            this.canvasDraft = canvasDraft == null ? "" : canvasDraft.trim();
        }
    }

    /**
     * Envía un mensaje en el chat de un relato.
     * 
     * @param relatoId ID del relato
     * @param usuarioId ID del usuario que envía el mensaje
     * @param emisor Tipo de emisor ('Usuario', 'Poly', 'Sistema')
     * @param contenido Contenido del mensaje
     * @return JsonObject con el resultado de la operación
     */
    public static JsonObject sendMessage(
            int relatoId,
            int usuarioId,
            String emisor,
            String contenido,
            JsonObject parametrosIA,
            JsonObject archivoContexto) {
        // Validaciones básicas
        if (relatoId <= 0 || usuarioId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "IDs inválidos");
            response.addProperty("status", 400);
            return response;
        }
        
        if (emisor == null || emisor.trim().isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El emisor es obligatorio");
            response.addProperty("status", 400);
            return response;
        }
        
        if (contenido == null || contenido.trim().isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El contenido del mensaje es obligatorio");
            response.addProperty("status", 400);
            return response;
        }
        
        // Validar que el emisor sea válido
        if (!emisor.equals("Usuario") && !emisor.equals("Poly") && !emisor.equals("Sistema")) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Emisor inválido. Debe ser 'Usuario', 'Poly' o 'Sistema'");
            response.addProperty("status", 400);
            return response;
        }
        
        // Validar longitud del contenido
        if (contenido.length() > 5000) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El mensaje no puede exceder 5000 caracteres");
            response.addProperty("status", 400);
            return response;
        }

        if (emisor.equals("Usuario")) {
            JsonObject moderationResult = ModerationService.validateText(
                    contenido,
                    usuarioId,
                    "Poly no admite contenido +18 o palabras bloqueadas. Ajusta el mensaje e inténtalo de nuevo.",
                    "Mensaje de chat bloqueado por moderación");
            if (moderationResult != null) {
                return moderationResult;
            }
        }
        
        JsonObject accessValidation = validateStoryOwnership(relatoId, usuarioId);
        if (accessValidation != null) {
            return accessValidation;
        }
        
        try {
            // Obtener el siguiente orden para el mensaje
            int siguienteOrden = getSiguienteOrden(relatoId);
            
            // Guardar el mensaje en la base de datos
            ChatDao.save(relatoId, emisor, contenido.trim(), siguienteOrden);
            
            // Si el mensaje es del usuario, generar respuesta de Poly
            if (emisor.equals("Usuario")) {
                generarRespuestaPoly(relatoId, usuarioId, contenido, parametrosIA, archivoContexto);
            }
            
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Mensaje enviado correctamente");
            response.addProperty("status", 200);
            return response;
            
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Error al enviar mensaje: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Obtiene el historial de chat de un relato.
     * 
     * @param relatoId ID del relato
     * @param usuarioId ID del usuario solicitante
     * @return JsonObject con el historial de mensajes
     */
    public static JsonObject getChatHistory(int relatoId, int usuarioId) {
        if (relatoId <= 0 || usuarioId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "IDs inválidos");
            response.addProperty("status", 400);
            return response;
        }
        
        JsonObject accessValidation = validateStoryOwnership(relatoId, usuarioId);
        if (accessValidation != null) {
            return accessValidation;
        }
        
        try {
            JsonObject result = ChatDao.listByStory(relatoId);
            
            // Enriquecer el resultado con información adicional
            if (result.get("status").getAsInt() == 200) {
                JsonObject response = new JsonObject();
                response.add("mensajes", result.get("mensajes"));
                response.addProperty("total", result.get("mensajes").getAsJsonArray().size());
                response.addProperty("relatoId", relatoId);
                response.addProperty("fechaUltimoMensaje", LocalDateTime.now().toString());
                response.addProperty("status", 200);
                return response;
            }
            
            return result;
            
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Error al obtener historial: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    /**
     * Genera una respuesta automática de Poly (IA).
     * 
     * @param relatoId ID del relato
     * @param mensajeUsuario Mensaje del usuario al que responder
     */
    private static void generarRespuestaPoly(
            int relatoId,
            int usuarioId,
            String mensajeUsuario,
            JsonObject parametrosIA,
            JsonObject archivoContexto) {
        try {
            PolyResponsePayload payload = buildFallbackPolyPayload(relatoId, usuarioId, mensajeUsuario, parametrosIA,
                    archivoContexto);
            String prompt = buildGeminiPrompt(relatoId, mensajeUsuario, archivoContexto);
            String instrucciones = buildGeminiInstructions(relatoId, usuarioId, parametrosIA);
            JsonObject geminiResponse = GeminiService.generateText(
                    prompt,
                    instrucciones,
                    buildModelCandidates(relatoId, usuarioId));

            if (geminiResponse.has("status") && geminiResponse.get("status").getAsInt() == 200
                    && geminiResponse.has("AI")) {
                payload = parsePolyResponse(geminiResponse.get("AI").getAsString());
            } else if (geminiResponse.has("Mensaje")) {
                System.err.println("Gemini no respondió para relato " + relatoId + ": "
                        + geminiResponse.get("Mensaje").getAsString()
                        + (geminiResponse.has("detalle") ? " | " + geminiResponse.get("detalle").getAsString() : ""));
            }
            
            // Obtener el siguiente orden para la respuesta
            int siguienteOrden = getSiguienteOrden(relatoId);
            
            // Guardar la respuesta de Poly
            ChatDao.save(relatoId, "Poly", sanitizeChatMessage(payload.chatMessage), siguienteOrden);

            if (!payload.canvasDraft.isBlank()) {
                StoryDao.updateDescription(relatoId, payload.canvasDraft);
            }
            
            System.out.println("Respuesta de Poly generada para relato " + relatoId);
            
        } catch (Exception e) {
            System.err.println("Error al generar respuesta de Poly: " + e.getMessage());
            
            // En caso de error, enviar un mensaje de sistema
            try {
                int siguienteOrden = getSiguienteOrden(relatoId);
                ChatDao.save(relatoId, "Sistema", "Lo siento, no pude generar una respuesta en este momento.", siguienteOrden);
            } catch (Exception ex) {
                System.err.println("Error al enviar mensaje de sistema: " + ex.getMessage());
            }
        }
    }

    private static String buildGeminiPrompt(int relatoId, String mensajeUsuario, JsonObject archivoContexto) {
        StringBuilder prompt = new StringBuilder();
        JsonObject storyResponse = StoryDao.findById(relatoId);

        if (storyResponse.has("status") && storyResponse.get("status").getAsInt() == 200
                && storyResponse.has("relato")) {
            JsonObject relato = storyResponse.getAsJsonObject("relato");
            prompt.append("Contexto del relato actual:\n");
            prompt.append("Titulo: ")
                    .append(relato.has("titulo") ? relato.get("titulo").getAsString() : "Sin titulo")
                    .append("\n");
            prompt.append("Modo: ")
                    .append(relato.has("modoOrigen") ? relato.get("modoOrigen").getAsString() : "Sin modo")
                    .append("\n");

            String descripcion = relato.has("descripcion") ? relato.get("descripcion").getAsString() : "";
            if (!descripcion.isBlank()) {
                prompt.append("Borrador actual:\n").append(descripcion).append("\n\n");
            }
        }

        JsonObject historyResponse = ChatDao.listByStory(relatoId);
        if (historyResponse.has("status") && historyResponse.get("status").getAsInt() == 200
                && historyResponse.has("mensajes")) {
            JsonArray mensajes = historyResponse.getAsJsonArray("mensajes");
            int fromIndex = Math.max(0, mensajes.size() - 6);
            prompt.append("Historial reciente:\n");

            for (int index = fromIndex; index < mensajes.size(); index++) {
                JsonElement item = mensajes.get(index);
                if (!item.isJsonObject()) {
                    continue;
                }

                JsonObject mensaje = item.getAsJsonObject();
                prompt.append("- ")
                        .append(mensaje.has("emisor") ? mensaje.get("emisor").getAsString() : "Sistema")
                        .append(": ")
                        .append(mensaje.has("contenido") ? mensaje.get("contenido").getAsString() : "")
                        .append("\n");
            }
        }

        if (archivoContexto != null && archivoContexto.has("contenido")) {
            String nombreArchivo = archivoContexto.has("nombre")
                    ? archivoContexto.get("nombre").getAsString()
                    : "archivo adjunto";
            String contenidoArchivo = archivoContexto.get("contenido").getAsString().trim();

            if (!contenidoArchivo.isBlank()) {
                prompt.append("\nArchivo adjunto del usuario (")
                        .append(nombreArchivo)
                        .append("):\n")
                        .append(contenidoArchivo)
                        .append("\n");
            }
        }

        JsonObject filesResponse = UploadedFileDao.listByStoryAndOrigin(relatoId, "Subido");
        if (filesResponse.has("status") && filesResponse.get("status").getAsInt() == 200
                && filesResponse.has("archivos")) {
            JsonArray files = filesResponse.getAsJsonArray("archivos");
            if (files.size() > 0) {
                prompt.append("\nArchivos fuente asociados al relato:\n");
                int addedFiles = 0;

                for (JsonElement item : files) {
                    if (!item.isJsonObject()) {
                        continue;
                    }

                    JsonObject file = item.getAsJsonObject();
                    String fileName = file.has("nombreArchivo") ? file.get("nombreArchivo").getAsString() : "Archivo";
                    String fileType = file.has("tipoArchivo") ? file.get("tipoArchivo").getAsString() : "Desconocido";
                    String storagePath = file.has("rutaAlmacenamiento")
                            ? file.get("rutaAlmacenamiento").getAsString()
                            : "";

                    prompt.append("- ")
                            .append(fileName)
                            .append(" [")
                            .append(fileType)
                            .append("]\n");

                    String extractedText = DocumentTextExtractor.extractText(storagePath, fileType);
                    if (!extractedText.isBlank()) {
                        prompt.append("Contenido extraído de ")
                                .append(fileName)
                                .append(":\n")
                                .append(extractedText)
                                .append("\n\n");
                    }

                    addedFiles++;
                    if (addedFiles >= MAX_SOURCE_FILES_IN_PROMPT) {
                        break;
                    }
                }
            }
        }

        prompt.append("\nUltimo mensaje del usuario:\n").append(mensajeUsuario).append("\n\n");
        prompt.append("Responde como Poly. Si ya hay suficiente contexto, desarrolla directamente el borrador de la historia en texto narrativo util para el canvas. ");
        prompt.append("Si aun falta contexto, guia al usuario con una respuesta breve pero orientada a construir el relato final.");

        return prompt.toString();
    }

    private static String buildGeminiInstructions(int relatoId, int usuarioId, JsonObject parametrosIA) {
        StringBuilder instrucciones = new StringBuilder();
        instrucciones.append("Eres Poly, un asistente de escritura creativa. ");
        instrucciones.append("Debes ayudar a construir relatos listos para convertirse en borrador final. ");
        instrucciones.append("CanvasAI debe recibir el texto final del relato, no la conversacion. ");
        instrucciones.append("Evita meta-explicaciones innecesarias y prioriza contenido narrativo cuando el usuario ya haya dado suficiente contexto. ");
        instrucciones.append("Si ya puedes escribir un borrador final, responde con este formato exacto:\n");
        instrucciones.append("[[CANVAS]]\n");
        instrucciones.append("texto narrativo final, continuo y listo para CanvasAI\n");
        instrucciones.append("[[/CANVAS]]\n");
        instrucciones.append("[[CHAT]]\n");
        instrucciones.append("una nota breve para el chat confirmando que actualizaste el canvas\n");
        instrucciones.append("[[/CHAT]]\n");
        instrucciones.append("Si todavia falta contexto, responde solo con el bloque [[CHAT]] y no incluyas [[CANVAS]]. ");
        instrucciones.append("Dentro de [[CANVAS]] no pongas encabezados, etiquetas ni explicaciones al usuario.");

        JsonObject effectiveSettings = buildEffectiveAISettings(relatoId, parametrosIA);
        String permanentInstruction = getPermanentInstruction(usuarioId);

        if (effectiveSettings.has("estiloEscritura")) {
            instrucciones.append("\nEstilo de escritura: ")
                    .append(effectiveSettings.get("estiloEscritura").getAsString())
                    .append(".");
        }

        if (effectiveSettings.has("nivelCreatividad")) {
            instrucciones.append("\nNivel de creatividad: ")
                    .append(effectiveSettings.get("nivelCreatividad").getAsString())
                    .append(".");
        }

        if (effectiveSettings.has("longitudRespuesta")) {
            instrucciones.append("\nLongitud de respuesta: ")
                    .append(effectiveSettings.get("longitudRespuesta").getAsString())
                    .append(".");
        }

        if (effectiveSettings.has("tonoEmocional")) {
            instrucciones.append("\nTono emocional: ")
                    .append(effectiveSettings.get("tonoEmocional").getAsString())
                    .append(".");
        }

        if (!permanentInstruction.isBlank()) {
            instrucciones.append("\n\nInstruccion permanente del usuario:\n").append(permanentInstruction);
        }

        return instrucciones.toString();
    }

    private static JsonObject buildEffectiveAISettings(int relatoId, JsonObject parametrosIA) {
        JsonObject effectiveSettings = new JsonObject();
        JsonObject savedSettings = AIConfigurationDao.findByStoryId(relatoId);
        if (savedSettings.has("status") && savedSettings.get("status").getAsInt() == 200
                && savedSettings.has("configuracionIA")) {
            JsonObject storedConfig = savedSettings.getAsJsonObject("configuracionIA");
            for (String key : new String[] { "estiloEscritura", "nivelCreatividad", "longitudRespuesta",
                    "tonoEmocional" }) {
                if (storedConfig.has(key)) {
                    effectiveSettings.add(key, storedConfig.get(key));
                }
            }
        }

        if (parametrosIA != null) {
            for (String key : new String[] { "estiloEscritura", "nivelCreatividad", "longitudRespuesta",
                    "tonoEmocional" }) {
                if (parametrosIA.has(key)) {
                    effectiveSettings.add(key, parametrosIA.get(key));
                }
            }
        }

        return effectiveSettings;
    }

    private static String getPermanentInstruction(int usuarioId) {
        JsonObject userInstructions = SettingsDao.getInstruccionIA(usuarioId);
        if (userInstructions.has("status") && userInstructions.get("status").getAsInt() == 200
                && userInstructions.has("instruccion")) {
            return userInstructions.get("instruccion").getAsString().trim();
        }

        return "";
    }

    private static List<String> buildModelCandidates(int relatoId, int usuarioId) {
        LinkedHashSet<String> modelCandidates = new LinkedHashSet<>();
        Integer requestedModelId = null;
        JsonObject storyResponse = StoryDao.findById(relatoId);
        if (storyResponse.has("status") && storyResponse.get("status").getAsInt() == 200
                && storyResponse.has("relato")) {
            JsonObject relato = storyResponse.getAsJsonObject("relato");
            if (relato.has("modeloUsadoId")) {
                requestedModelId = relato.get("modeloUsadoId").getAsInt();
            }
        }

        JsonObject resolvedModel = SettingsService.resolveModeloIA(usuarioId, requestedModelId, false);
        if (resolvedModel.has("status") && resolvedModel.get("status").getAsInt() == 200) {
            if (resolvedModel.has("modelo") && resolvedModel.get("modelo").isJsonObject()) {
                addModelCandidate(modelCandidates, resolvedModel.getAsJsonObject("modelo"));
            }

            if (resolvedModel.has("modelos") && resolvedModel.get("modelos").isJsonArray()) {
                for (JsonElement item : resolvedModel.getAsJsonArray("modelos")) {
                    if (item.isJsonObject()) {
                        addModelCandidate(modelCandidates, item.getAsJsonObject());
                    }
                }
            }
        }

        return new ArrayList<>(modelCandidates);
    }

    private static void addModelCandidate(LinkedHashSet<String> candidates, JsonObject model) {
        if (model == null || !model.has("nombre")) {
            return;
        }

        String candidate = model.get("nombre").getAsString().trim();
        if (!candidate.isBlank()) {
            candidates.add(candidate);
        }
    }

    private static PolyResponsePayload parsePolyResponse(String rawResponse) {
        String normalized = String.valueOf(rawResponse == null ? "" : rawResponse).trim();
        if (normalized.isBlank()) {
            return new PolyResponsePayload(
                    "No pude generar un texto final en este intento, pero puedes volver a pedirme que continúe la historia.",
                    "");
        }

        String canvasDraft = sanitizeCanvasDraft(extractTaggedBlock(normalized, "CANVAS"));
        String chatMessage = sanitizeChatMessage(extractTaggedBlock(normalized, "CHAT"));

        if (!canvasDraft.isBlank()) {
            return new PolyResponsePayload(
                    chatMessage.isBlank() ? CANVAS_UPDATE_MESSAGE : chatMessage,
                    canvasDraft);
        }

        if (!chatMessage.isBlank()) {
            return new PolyResponsePayload(chatMessage, "");
        }

        if (looksLikeCanvasDraft(normalized)) {
            return new PolyResponsePayload(CANVAS_UPDATE_MESSAGE, sanitizeCanvasDraft(normalized));
        }

        return new PolyResponsePayload(sanitizeChatMessage(normalized), "");
    }

    private static String extractTaggedBlock(String text, String tag) {
        if (text == null || tag == null || tag.isBlank()) {
            return "";
        }

        Pattern blockPattern = Pattern.compile(
                "\\[\\[" + Pattern.quote(tag) + "\\]\\](.*?)\\[\\[/"
                        + Pattern.quote(tag) + "\\]\\]",
                Pattern.DOTALL);
        Matcher matcher = blockPattern.matcher(text);
        if (!matcher.find()) {
            return "";
        }

        return matcher.group(1).trim();
    }

    private static boolean looksLikeCanvasDraft(String text) {
        String normalized = String.valueOf(text == null ? "" : text).trim();
        if (normalized.isBlank()) {
            return false;
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        String[] metaStarts = {
                "puedo ", "si quieres", "te propongo", "para ", "voy a ",
                "lo mas util", "lo más útil", "he actualizado", "puedes "
        };
        for (String metaStart : metaStarts) {
            if (lower.startsWith(metaStart)) {
                return false;
            }
        }

        return countWords(normalized) >= 90
                || normalized.contains("\n\n")
                || normalized.length() >= 520;
    }

    private static PolyResponsePayload buildFallbackPolyPayload(int relatoId, int usuarioId, String mensajeUsuario,
            JsonObject parametrosIA, JsonObject archivoContexto) {
        JsonObject effectiveSettings = buildEffectiveAISettings(relatoId, parametrosIA);
        String permanentInstruction = getPermanentInstruction(usuarioId);
        applyInstructionOverrides(effectiveSettings, permanentInstruction);

        String writingStyle = getSettingValue(effectiveSettings, "estiloEscritura", "Narrativo");
        String responseLength = getSettingValue(effectiveSettings, "longitudRespuesta", "Media");
        String emotionalTone = getSettingValue(effectiveSettings, "tonoEmocional", "Neutral");
        String normalizedMessage = summarizeText(mensajeUsuario, 180);
        String draft = getStoryDraft(relatoId);
        String sourceHint = getSourceContextHint(relatoId, archivoContexto);
        boolean continueStory = wantsStoryContinuation(mensajeUsuario, draft, sourceHint);

        if (continueStory) {
            String nextDraft = buildFallbackNarrativeDraft(
                    writingStyle,
                    emotionalTone,
                    responseLength,
                    normalizedMessage,
                    draft,
                    sourceHint,
                    permanentInstruction);
            return new PolyResponsePayload(
                    "Actualice el canvas con un borrador narrativo para que lo revises y lo conviertas si te sirve.",
                    nextDraft);
        }

        return new PolyResponsePayload(
                buildGuidanceChatMessage(writingStyle, responseLength, emotionalTone, normalizedMessage, sourceHint,
                        permanentInstruction),
                "");
    }

    private static void applyInstructionOverrides(JsonObject effectiveSettings, String permanentInstruction) {
        String normalized = normalizeInstruction(permanentInstruction);
        if (normalized.isBlank()) {
            return;
        }

        if (normalized.contains("dialogado")) {
            effectiveSettings.addProperty("estiloEscritura", "Dialogado");
        } else if (normalized.contains("descriptivo")) {
            effectiveSettings.addProperty("estiloEscritura", "Descriptivo");
        } else if (normalized.contains("narrativo")) {
            effectiveSettings.addProperty("estiloEscritura", "Narrativo");
        }

        if (normalized.contains("poetico") || normalized.contains("poético")) {
            effectiveSettings.addProperty("tonoEmocional", "Poético");
        } else if (normalized.contains("dramatico") || normalized.contains("dramático")) {
            effectiveSettings.addProperty("tonoEmocional", "Dramático");
        } else if (normalized.contains("neutral")) {
            effectiveSettings.addProperty("tonoEmocional", "Neutral");
        }

        if (normalized.contains("breve") || normalized.contains("corta")) {
            effectiveSettings.addProperty("longitudRespuesta", "Corta");
        } else if (normalized.contains("larga") || normalized.contains("extensa")
                || normalized.contains("detallada")) {
            effectiveSettings.addProperty("longitudRespuesta", "Larga");
        }
    }

    private static String normalizeInstruction(String instruction) {
        return String.valueOf(instruction == null ? "" : instruction).trim().toLowerCase(Locale.ROOT);
    }

    private static String getSettingValue(JsonObject settings, String key, String defaultValue) {
        if (settings != null && settings.has(key)) {
            return settings.get(key).getAsString();
        }

        return defaultValue;
    }

    private static String getStoryDraft(int relatoId) {
        JsonObject storyResponse = StoryDao.findById(relatoId);
        if (storyResponse.has("status") && storyResponse.get("status").getAsInt() == 200
                && storyResponse.has("relato")) {
            JsonObject relato = storyResponse.getAsJsonObject("relato");
            if (relato.has("descripcion")) {
                return relato.get("descripcion").getAsString().trim();
            }
        }

        return "";
    }

    private static String getSourceContextHint(int relatoId, JsonObject archivoContexto) {
        if (archivoContexto != null && archivoContexto.has("nombre")) {
            return archivoContexto.get("nombre").getAsString().trim();
        }

        JsonObject filesResponse = UploadedFileDao.listByStoryAndOrigin(relatoId, "Subido");
        if (filesResponse.has("status") && filesResponse.get("status").getAsInt() == 200
                && filesResponse.has("archivos")) {
            JsonArray files = filesResponse.getAsJsonArray("archivos");
            if (files.size() > 0 && files.get(0).isJsonObject()) {
                JsonObject file = files.get(0).getAsJsonObject();
                if (file.has("nombreArchivo")) {
                    return file.get("nombreArchivo").getAsString().trim();
                }
            }
        }

        return "";
    }

    private static boolean wantsStoryContinuation(String mensajeUsuario, String draft, String sourceHint) {
        String normalized = String.valueOf(mensajeUsuario == null ? "" : mensajeUsuario).trim().toLowerCase(Locale.ROOT);
        if (!draft.isBlank() || !sourceHint.isBlank()) {
            return true;
        }

        return normalized.length() > 90
                || normalized.contains("continua")
                || normalized.contains("continuar")
                || normalized.contains("escribe")
                || normalized.contains("desarrolla")
                || normalized.contains("escena")
                || normalized.contains("parrafo")
                || normalized.contains("capitulo")
                || normalized.contains("dialogo");
    }

    private static String buildGuidanceBody(String writingStyle, String userIdea, String sourceHint) {
        if (!sourceHint.isBlank()) {
            return "Con el material de " + sourceHint + ", lo mas util ahora es definir con precision la escena o el conflicto que quieres reforzar a partir de: "
                    + userIdea + ".";
        }

        if ("Dialogado".equalsIgnoreCase(writingStyle)) {
            return "Para llevar tu idea a un fragmento dialogado, conviene decidir quien inicia la escena, que tension hay entre voces y que informacion se revela primero alrededor de: "
                    + userIdea + ".";
        }

        if ("Descriptivo".equalsIgnoreCase(writingStyle)) {
            return "Para que la escena funcione en clave descriptiva, primero conviene fijar la imagen dominante, el ambiente y el detalle fisico mas potente alrededor de: "
                    + userIdea + ".";
        }

        return "Para convertir tu idea en narracion continua, lo mas util ahora es fijar un protagonista, una accion inmediata y una consecuencia clara alrededor de: "
                + userIdea + ".";
    }

    private static String buildGuidanceChatMessage(String writingStyle, String responseLength, String emotionalTone,
            String userIdea, String sourceHint, String permanentInstruction) {
        List<String> parts = new ArrayList<>();
        parts.add(getGuidanceLead(emotionalTone));
        parts.add(buildGuidanceBody(writingStyle, userIdea, sourceHint));
        parts.add(buildLengthClose(responseLength, false));

        if (!permanentInstruction.isBlank()) {
            parts.add("Mantendre activas tus instrucciones generales mientras seguimos trabajando.");
        }

        return sanitizeTextWithInstruction(String.join(" ", parts), permanentInstruction);
    }

    private static String getGuidanceLead(String emotionalTone) {
        if ("Dramático".equalsIgnoreCase(emotionalTone)) {
            return "Voy a orientar la escena hacia un conflicto mas tenso antes de llevarla al canvas.";
        }

        if ("Poético".equalsIgnoreCase(emotionalTone)) {
            return "Voy a sostener la escena con una cadencia mas evocadora antes de cerrarla como borrador final.";
        }

        return "Voy a mantener la respuesta clara y orientada al relato antes de pasarla al canvas.";
    }

    private static String buildLengthClose(String responseLength, boolean continueStory) {
        if ("Corta".equalsIgnoreCase(responseLength)) {
            return continueStory
                    ? "Si quieres, en el siguiente mensaje lo cierro en un parrafo breve listo para el canvas."
                    : "Si quieres, en el siguiente mensaje lo bajo a una version breve y directa.";
        }

        if ("Larga".equalsIgnoreCase(responseLength)) {
            return continueStory
                    ? "Si quieres, en el siguiente paso lo convierto en un fragmento mas extenso con apertura, desarrollo y cierre provisional."
                    : "Si quieres, en el siguiente paso desarrollo esta base con mas detalle, ritmo y continuidad narrativa.";
        }

        return continueStory
                ? "Si quieres, en el siguiente mensaje lo convierto en dos o tres parrafos continuos listos para el canvas."
                : "Si quieres, en el siguiente mensaje lo desarrollo en una respuesta intermedia ya orientada al borrador final.";
    }

    private static String buildFallbackNarrativeDraft(String writingStyle, String emotionalTone, String responseLength,
            String userIdea, String draft, String sourceHint, String permanentInstruction) {
        String normalizedIdea = userIdea == null || userIdea.isBlank()
                ? "la escena pendiente del relato"
                : userIdea;
        List<String> paragraphs = new ArrayList<>();

        paragraphs.add(buildNarrativeOpening(writingStyle, emotionalTone, normalizedIdea, sourceHint, draft));
        paragraphs.add(buildNarrativeDevelopment(writingStyle, emotionalTone, normalizedIdea, sourceHint));

        if ("Larga".equalsIgnoreCase(responseLength) || "Media".equalsIgnoreCase(responseLength)) {
            paragraphs.add(buildNarrativeClosure(writingStyle, emotionalTone, normalizedIdea));
        }

        String generatedDraft = String.join("\n\n", paragraphs);
        String combinedDraft = draft == null || draft.isBlank()
                ? generatedDraft
                : draft.trim() + "\n\n" + generatedDraft;

        return sanitizeTextWithInstruction(combinedDraft, permanentInstruction);
    }

    private static String buildNarrativeOpening(String writingStyle, String emotionalTone, String userIdea,
            String sourceHint, String draft) {
        String toneFragment = getToneSceneFragment(emotionalTone);
        String baseContext = !draft.isBlank()
                ? "La historia retomo el pulso del momento anterior sin perder la tension que ya venia acumulando."
                : !sourceHint.isBlank()
                        ? "La historia tomo forma sobre la huella que dejo " + sourceHint + ", como si ese material hubiera abierto una puerta concreta dentro de la escena."
                        : "La escena comenzo a cerrarse alrededor de " + userIdea + ", con una direccion ya clara para el relato.";

        if ("Dialogado".equalsIgnoreCase(writingStyle)) {
            return baseContext + " " + toneFragment + " \"No podemos seguir fingiendo que nada cambio\", dijo uno de los personajes, y en esa frase quedo expuesto el nucleo del conflicto.";
        }

        if ("Descriptivo".equalsIgnoreCase(writingStyle)) {
            return baseContext + " " + toneFragment + " El espacio se lleno de detalles precisos, de texturas y señales que empujaban la mirada hacia el centro del problema.";
        }

        return baseContext + " " + toneFragment + " Cada movimiento empujo la accion hacia adelante hasta dejar claro que ya no habia regreso posible.";
    }

    private static String buildNarrativeDevelopment(String writingStyle, String emotionalTone, String userIdea,
            String sourceHint) {
        String conflictFragment = !sourceHint.isBlank()
                ? "Lo que parecia una referencia de apoyo se convirtio en una pieza decisiva para interpretar " + userIdea + "."
                : "La tension de " + userIdea + " dejo de ser una idea suelta y paso a convertirse en una consecuencia visible dentro del relato.";

        if ("Dialogado".equalsIgnoreCase(writingStyle)) {
            return conflictFragment + " \"Si damos un paso mas, ya no vamos a poder volver\", respondio la otra voz, y el silencio posterior termino de fijar el costo emocional de la escena.";
        }

        if ("Descriptivo".equalsIgnoreCase(writingStyle)) {
            return conflictFragment + " El ambiente se volvio mas denso, mas concreto, y cada detalle sensorial reforzo la sensacion de que la historia estaba cruzando un umbral.";
        }

        if ("Poético".equalsIgnoreCase(emotionalTone)) {
            return conflictFragment + " La escena avanzo con un ritmo contenido, dejando que cada imagen respirara antes de caer sobre la siguiente como una marea lenta.";
        }

        if ("Dramático".equalsIgnoreCase(emotionalTone)) {
            return conflictFragment + " El conflicto escalo con rapidez, dejando a los personajes frente a una decision que ya no admitia demora.";
        }

        return conflictFragment + " A partir de ahi, la secuencia gano continuidad y sostuvo el avance de la historia sin desviarse del eje principal.";
    }

    private static String buildNarrativeClosure(String writingStyle, String emotionalTone, String userIdea) {
        if ("Dialogado".equalsIgnoreCase(writingStyle)) {
            return "\"Entonces hagamos que esta vez importe\", dijo la voz que hasta ese momento habia dudado, y la escena quedo cerrada con una resolucion provisional que invita a seguir el siguiente tramo del relato.";
        }

        if ("Descriptivo".equalsIgnoreCase(writingStyle)) {
            return "Cuando la escena termino de asentarse, el entorno y las emociones ya hablaban el mismo idioma, dejando a " + userIdea + " integrado como una imagen final lista para seguir desarrollandose.";
        }

        if ("Poético".equalsIgnoreCase(emotionalTone)) {
            return "Al final, la escena quedo suspendida en un equilibrio fragil, como si el relato hubiera encontrado una forma de respirar justo antes del siguiente giro.";
        }

        if ("Dramático".equalsIgnoreCase(emotionalTone)) {
            return "El cierre dejo una sensacion de urgencia contenida y una consecuencia clara, suficiente para que el siguiente tramo del relato arranque sin perder impulso.";
        }

        return "Con eso, el fragmento quedo cerrado como una unidad narrativa clara, listo para seguir creciendo en el canvas como parte del relato final.";
    }

    private static String getToneSceneFragment(String emotionalTone) {
        if ("Poético".equalsIgnoreCase(emotionalTone)) {
            return "Habia en el ambiente una cadencia leve, casi suspendida, que hacia sentir cada gesto como una señal irrepetible.";
        }

        if ("Dramático".equalsIgnoreCase(emotionalTone)) {
            return "Todo quedo atravesado por una tension inmediata, como si cualquier minimo error pudiera romper el equilibrio de la escena.";
        }

        return "La escena sostuvo un tono firme y continuo, suficiente para que el lector entrara de inmediato en el conflicto.";
    }

    private static String sanitizeTextWithInstruction(String response, String permanentInstruction) {
        String sanitized = response == null ? "" : response.trim();
        if (sanitized.isBlank()) {
            return "Puedo seguir ayudandote con el relato en cuanto me des una escena, conflicto o personaje para desarrollar.";
        }

        Pattern quotedPattern = Pattern.compile("(?i)(?:nunca|no) uses? la palabra\\s+[\"“']([^\"”']+)[\"”']");
        Matcher quotedMatcher = quotedPattern.matcher(String.valueOf(permanentInstruction == null ? "" : permanentInstruction));
        while (quotedMatcher.find()) {
            sanitized = sanitized.replaceAll("(?i)\\b" + Pattern.quote(quotedMatcher.group(1).trim()) + "\\b", "");
        }

        return sanitized.replaceAll("\\s{2,}", " ").trim();
    }

    private static String sanitizeCanvasDraft(String text) {
        String sanitized = String.valueOf(text == null ? "" : text).trim();
        sanitized = sanitized.replaceAll("(?im)^borrador final\\s*:?\\s*", "");
        return sanitized.trim();
    }

    private static String sanitizeChatMessage(String text) {
        String sanitized = String.valueOf(text == null ? "" : text).trim();
        if (sanitized.isBlank()) {
            return CANVAS_UPDATE_MESSAGE;
        }

        sanitized = sanitized.replaceAll("\\s{2,}", " ").trim();
        return sanitized.length() > 500 ? sanitized.substring(0, 500).trim() : sanitized;
    }

    private static String summarizeText(String text, int maxLength) {
        String normalized = String.valueOf(text == null ? "" : text).trim().replaceAll("\\s+", " ");
        if (normalized.length() <= maxLength) {
            return normalized;
        }

        return normalized.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
    }

    private static int countWords(String text) {
        String normalized = String.valueOf(text == null ? "" : text).trim();
        if (normalized.isBlank()) {
            return 0;
        }

        return normalized.split("\\s+").length;
    }

    /**
     * Obtiene el siguiente número de orden para un relato.
     * 
     * @param relatoId ID del relato
     * @return Siguiente número de orden
     */
    private static int getSiguienteOrden(int relatoId) {
        try {
            JsonObject historial = ChatDao.listByStory(relatoId);
            
            if (historial.get("status").getAsInt() == 200) {
                int totalMensajes = historial.get("mensajes").getAsJsonArray().size();
                return totalMensajes + 1;
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener siguiente orden: " + e.getMessage());
        }
        
        return 1; // Por defecto, si no hay mensajes o hay error
    }

    /**
     * Limpia el historial de chat de un relato.
     * 
     * @param relatoId ID del relato
     * @param usuarioId ID del usuario solicitante
     * @return JsonObject con el resultado
     */
    public static JsonObject clearChatHistory(int relatoId, int usuarioId) {
        if (relatoId <= 0 || usuarioId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "IDs inválidos");
            response.addProperty("status", 400);
            return response;
        }
        
        JsonObject accessValidation = validateStoryOwnership(relatoId, usuarioId);
        if (accessValidation != null) {
            return accessValidation;
        }

        return ChatDao.deleteByStory(relatoId);
    }

    /**
     * Obtiene estadísticas del chat de un relato.
     * 
     * @param relatoId ID del relato
     * @param usuarioId ID del usuario solicitante
     * @return JsonObject con estadísticas
     */
    public static JsonObject getChatStats(int relatoId, int usuarioId) {
        if (relatoId <= 0 || usuarioId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "IDs inválidos");
            response.addProperty("status", 400);
            return response;
        }

        JsonObject accessValidation = validateStoryOwnership(relatoId, usuarioId);
        if (accessValidation != null) {
            return accessValidation;
        }
        
        try {
            JsonObject historial = ChatDao.listByStory(relatoId);
            JsonObject stats = new JsonObject();
            
            if (historial.get("status").getAsInt() == 200) {
                int totalMensajes = historial.get("mensajes").getAsJsonArray().size();
                int mensajesUsuario = 0;
                int mensajesPoly = 0;
                int mensajesSistema = 0;
                
                // Contar mensajes por tipo
                for (int i = 0; i < totalMensajes; i++) {
                    JsonObject mensaje = historial.get("mensajes").getAsJsonArray().get(i).getAsJsonObject();
                    String emisor = mensaje.get("emisor").getAsString();
                    
                    switch (emisor) {
                        case "Usuario":
                            mensajesUsuario++;
                            break;
                        case "Poly":
                            mensajesPoly++;
                            break;
                        case "Sistema":
                            mensajesSistema++;
                            break;
                    }
                }
                
                stats.addProperty("totalMensajes", totalMensajes);
                stats.addProperty("mensajesUsuario", mensajesUsuario);
                stats.addProperty("mensajesPoly", mensajesPoly);
                stats.addProperty("mensajesSistema", mensajesSistema);
                stats.addProperty("relatoId", relatoId);
                stats.addProperty("fechaConsulta", LocalDateTime.now().toString());
                
                JsonObject response = new JsonObject();
                response.add("estadisticas", stats);
                response.addProperty("status", 200);
                return response;
            }
            
            return historial;
            
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Error al obtener estadísticas: " + e.getMessage());
            response.addProperty("status", 500);
            return response;
        }
    }

    private static JsonObject validateStoryOwnership(int relatoId, int usuarioId) {
        JsonObject storyResult = StoryDao.findById(relatoId);

        if (!storyResult.has("status")) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "No fue posible validar el relato");
            response.addProperty("status", 500);
            return response;
        }

        int storyStatus = storyResult.get("status").getAsInt();
        if (storyStatus != 200) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", storyResult.has("Mensaje")
                    ? storyResult.get("Mensaje").getAsString()
                    : "Relato no encontrado");
            response.addProperty("status", storyStatus);
            return response;
        }

        JsonObject relato = storyResult.getAsJsonObject("relato");
        if (relato.get("usuarioId").getAsInt() != usuarioId) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "No tienes permisos para acceder a este relato");
            response.addProperty("status", 403);
            return response;
        }

        return null;
    }
}
