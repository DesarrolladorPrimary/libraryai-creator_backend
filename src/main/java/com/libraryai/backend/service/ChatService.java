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
    private static final String CANVAS_UPDATE_MESSAGE_VARIANT = "Anadi un avance nuevo al canvas para que revises el relato.";

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
            String previousDraft = getStoryDraft(relatoId);
            String prompt = buildGeminiPrompt(relatoId, mensajeUsuario, archivoContexto);
            String instrucciones = buildGeminiInstructions(relatoId, usuarioId, parametrosIA);
            JsonObject geminiResponse = GeminiService.generateText(
                    prompt,
                    instrucciones,
                    buildModelCandidates(relatoId, usuarioId));

            PolyResponsePayload payload = null;

            if (geminiResponse.has("status") && geminiResponse.get("status").getAsInt() == 200
                    && geminiResponse.has("AI")) {
                payload = parsePolyResponse(geminiResponse.get("AI").getAsString());
            } else if (geminiResponse.has("Mensaje")) {
                System.err.println("Gemini no respondio para relato " + relatoId + ": "
                        + geminiResponse.get("Mensaje").getAsString()
                        + (geminiResponse.has("detalle") ? " | " + geminiResponse.get("detalle").getAsString() : ""));
            }

            if (!isUsablePolyPayload(payload)) {
                int siguienteOrden = getSiguienteOrden(relatoId);
                ChatDao.save(relatoId, "Sistema",
                        "Poly no pudo generar una respuesta en este intento. Intenta de nuevo en unos segundos o reformula la instrucción.",
                        siguienteOrden);
                return;
            }

            int siguienteOrden = getSiguienteOrden(relatoId);
            String chatMessage = payload.canvasDraft.isBlank()
                    ? sanitizeChatMessage(payload.chatMessage)
                    : buildCanvasUpdateMessage(
                            payload.chatMessage,
                            mensajeUsuario,
                            previousDraft,
                            mergeCanvasDraft(previousDraft, payload.canvasDraft));
            ChatDao.save(relatoId, "Poly", chatMessage, siguienteOrden);

            if (!payload.canvasDraft.isBlank()) {
                String mergedDraft = mergeCanvasDraft(previousDraft, payload.canvasDraft);
                if (!mergedDraft.isBlank() && !mergedDraft.equals(previousDraft)) {
                    StoryDao.updateDescription(relatoId, mergedDraft);
                }
            }

            System.out.println("Respuesta de Poly generada para relato " + relatoId);

        } catch (Exception e) {
            System.err.println("Error al generar respuesta de Poly: " + e.getMessage());

            try {
                int siguienteOrden = getSiguienteOrden(relatoId);
                ChatDao.save(relatoId, "Sistema",
                        "Poly no pudo generar una respuesta en este intento. Intenta de nuevo en unos segundos o reformula la instrucción.",
                        siguienteOrden);
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
            prompt.append("Título: ")
                    .append(relato.has("titulo") ? relato.get("titulo").getAsString() : "Sin título")
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
            List<String> meaningfulHistory = new ArrayList<>();

            for (int index = fromIndex; index < mensajes.size(); index++) {
                JsonElement item = mensajes.get(index);
                if (!item.isJsonObject()) {
                    continue;
                }

                JsonObject mensaje = item.getAsJsonObject();
                String emisor = mensaje.has("emisor") ? mensaje.get("emisor").getAsString() : "Sistema";
                String contenido = mensaje.has("contenido") ? mensaje.get("contenido").getAsString() : "";

                if (isLowSignalHistoryMessage(emisor, contenido)) {
                    continue;
                }

                meaningfulHistory.add("- " + emisor + ": " + contenido);
            }

            int startHistory = Math.max(0, meaningfulHistory.size() - 4);
            for (int index = startHistory; index < meaningfulHistory.size(); index++) {
                prompt.append(meaningfulHistory.get(index)).append("\n");
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
                        prompt.append("Contenido extraido de ")
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
        prompt.append("Responde como Poly. Si ya hay suficiente contexto, escribe el siguiente fragmento narrativo que continue el borrador actual como una novela. ");
        prompt.append("No reescribas desde cero el texto previo ni repitas escenas ya resueltas. ");
        prompt.append("Si aun falta contexto, guia al usuario con una respuesta breve pero orientada a construir el relato final.");

        return prompt.toString();
    }

    private static String buildGeminiInstructions(int relatoId, int usuarioId, JsonObject parametrosIA) {
        StringBuilder instrucciones = new StringBuilder();
        instrucciones.append("Eres Poly, un asistente de escritura creativa. ");
        instrucciones.append("Debes ayudar a construir relatos listos para convertirse en borrador final. ");
        instrucciones.append("CanvasAI debe recibir la continuación narrativa del relato, no la conversación. ");
        instrucciones.append("Evita meta-explicaciones innecesarias y prioriza contenido narrativo cuando el usuario ya haya dado suficiente contexto. ");
        instrucciones.append("Si ya existe borrador, debes continuarlo como una novela y agregar solo el tramo nuevo. ");
        instrucciones.append("No reescribas desde cero el contenido existente, no lo resumes y no lo sustituyas por otra versión completa. ");
        instrucciones.append("Si ya puedes escribir un avance narrativo, responde con este formato exacto:\n");
        instrucciones.append("[[CANVAS]]\n");
        instrucciones.append("solo el nuevo fragmento narrativo que debe agregarse al final del borrador actual\n");
        instrucciones.append("[[/CANVAS]]\n");
        instrucciones.append("[[CHAT]]\n");
        instrucciones.append("una nota breve para el chat confirmando que actualizaste el canvas\n");
        instrucciones.append("[[/CHAT]]\n");
        instrucciones.append("Si todavía falta contexto, responde solo con el bloque [[CHAT]] y no incluyas [[CANVAS]]. ");
        instrucciones.append("Dentro de [[CANVAS]] no pongas encabezados, etiquetas ni explicaciones al usuario.");
        instrucciones.append(" Mantén continuidad de voz, personajes, tono, tiempo verbal y hechos previos.");
        instrucciones.append(" No repitas literalmente frases de confirmación anteriores ni contestes con el mismo texto en mensajes consecutivos.");
        instrucciones.append(" Si incluyes [[CHAT]], debe mencionar el avance concreto realizado y evitar fórmulas genéricas repetidas.");

        JsonObject effectiveSettings = buildEffectiveAISettings(relatoId, usuarioId, parametrosIA);
        String permanentInstruction = getPermanentInstruction(usuarioId);
        boolean premiumUser = SettingsService.isPremiumUser(usuarioId);

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
            instrucciones.append("\n\nInstrucción permanente del usuario:\n").append(permanentInstruction);
        }

        if (!premiumUser) {
            instrucciones.append(
                    "\n\nLimitación de plan Gratuito: mantén las respuestas compactas y prioriza avances breves.");
            instrucciones.append(
                    " Si generas [[CANVAS]], entrega un borrador corto y enfocado. No excedas aproximadamente 220 palabras en total.");
        }

        return instrucciones.toString();
    }

    private static JsonObject buildEffectiveAISettings(int relatoId, int usuarioId, JsonObject parametrosIA) {
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

        if (!SettingsService.isPremiumUser(usuarioId)) {
            effectiveSettings.addProperty("longitudRespuesta", "Corta");
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
            return new PolyResponsePayload("", "");
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

    private static boolean isUsablePolyPayload(PolyResponsePayload payload) {
        if (payload == null) {
            return false;
        }

        return (payload.chatMessage != null && !payload.chatMessage.isBlank())
                || (payload.canvasDraft != null && !payload.canvasDraft.isBlank());
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
                "lo mas util", "he actualizado", "puedes "
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
    private static boolean isLowSignalHistoryMessage(String emisor, String contenido) {
        String normalizedSender = String.valueOf(emisor == null ? "" : emisor).trim();
        String normalizedContent = String.valueOf(contenido == null ? "" : contenido).trim().toLowerCase(Locale.ROOT);

        if (normalizedContent.isBlank()) {
            return true;
        }

        if ("Sistema".equalsIgnoreCase(normalizedSender)
                && normalizedContent.contains("no pude generar una respuesta")) {
            return true;
        }

        if ("Poly".equalsIgnoreCase(normalizedSender)
                && (normalizedContent.equals(CANVAS_UPDATE_MESSAGE.toLowerCase(Locale.ROOT))
                        || normalizedContent.equals(CANVAS_UPDATE_MESSAGE_VARIANT.toLowerCase(Locale.ROOT))
                        || normalizedContent.startsWith("he actualizado el canvas")
                        || normalizedContent.startsWith("anadi un avance nuevo al canvas"))) {
            return true;
        }

        return false;
    }

    private static String buildCanvasUpdateMessage(String chatMessage, String userMessage, String previousDraft,
            String nextDraft) {
        String normalizedChat = String.valueOf(chatMessage == null ? "" : chatMessage).trim();
        if (!normalizedChat.isBlank()
                && !normalizedChat.equalsIgnoreCase(CANVAS_UPDATE_MESSAGE)
                && !normalizedChat.equalsIgnoreCase(CANVAS_UPDATE_MESSAGE_VARIANT)
                && !normalizedChat.toLowerCase(Locale.ROOT).startsWith("he actualizado el canvas")) {
            return sanitizeChatMessage(normalizedChat);
        }

        boolean hadDraft = previousDraft != null && !previousDraft.isBlank();
        int previousWords = countWords(previousDraft);
        int nextWords = countWords(nextDraft);
        String normalizedUserMessage = String.valueOf(userMessage == null ? "" : userMessage).trim().toLowerCase(Locale.ROOT);

        if (!hadDraft) {
            return "Deje un primer borrador en el canvas para que lo revises y me digas como seguir.";
        }

        if (normalizedUserMessage.contains("dialogo")) {
            return "Anadi un tramo nuevo al canvas con mas enfasis en el dialogo y el conflicto.";
        }

        if (normalizedUserMessage.contains("continua") || normalizedUserMessage.contains("continuar")
                || normalizedUserMessage.contains("sigue") || normalizedUserMessage.contains("seguir")) {
            return "Continue el relato en el canvas con un tramo nuevo para mantener el avance de la escena.";
        }

        if (nextWords > previousWords) {
            return "Extendi el borrador del canvas con un avance nuevo y mas desarrollo narrativo.";
        }

        return "Actualicé el canvas con una nueva versión del borrador para que no repitamos el mismo tramo.";
    }
    private static String sanitizeCanvasDraft(String text) {
        String sanitized = String.valueOf(text == null ? "" : text).trim();
        sanitized = sanitized.replaceAll("(?im)^borrador final\\s*:?\\s*", "");
        return sanitized.trim();
    }

    private static String mergeCanvasDraft(String previousDraft, String nextFragment) {
        String previous = sanitizeCanvasDraft(previousDraft);
        String candidate = sanitizeCanvasDraft(nextFragment);

        if (candidate.isBlank()) {
            return previous;
        }

        if (previous.isBlank()) {
            return candidate;
        }

        if (previous.equals(candidate)) {
            return previous;
        }

        List<String> mergedParagraphs = new ArrayList<>(splitNarrativeParagraphs(previous));
        LinkedHashSet<String> seenParagraphs = new LinkedHashSet<>();
        for (String paragraph : mergedParagraphs) {
            String key = normalizeNarrativeKey(paragraph);
            if (!key.isBlank()) {
                seenParagraphs.add(key);
            }
        }

        List<String> newParagraphs = new ArrayList<>();
        for (String paragraph : splitNarrativeParagraphs(candidate)) {
            String key = normalizeNarrativeKey(paragraph);
            if (key.isBlank() || seenParagraphs.contains(key)) {
                continue;
            }

            newParagraphs.add(paragraph);
            seenParagraphs.add(key);
        }

        if (newParagraphs.isEmpty()) {
            String suffix = extractIncrementalSuffix(previous, candidate);
            if (suffix.isBlank()) {
                return previous;
            }

            newParagraphs.addAll(splitNarrativeParagraphs(suffix));
        }

        if (newParagraphs.isEmpty()) {
            return previous;
        }

        mergedParagraphs.addAll(newParagraphs);
        return String.join("\n\n", mergedParagraphs).trim();
    }

    private static List<String> splitNarrativeParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();
        String normalized = String.valueOf(text == null ? "" : text).trim();
        if (normalized.isBlank()) {
            return paragraphs;
        }

        for (String paragraph : normalized.split("\\n\\s*\\n")) {
            String cleaned = paragraph.trim();
            if (!cleaned.isBlank()) {
                paragraphs.add(cleaned);
            }
        }

        if (paragraphs.isEmpty()) {
            paragraphs.add(normalized);
        }

        return paragraphs;
    }

    private static String extractIncrementalSuffix(String previous, String candidate) {
        String previousNormalized = normalizeNarrativeKey(previous);
        String candidateNormalized = normalizeNarrativeKey(candidate);
        if (previousNormalized.isBlank() || candidateNormalized.isBlank()) {
            return "";
        }

        if (candidate.startsWith(previous)) {
            return candidate.substring(previous.length()).trim();
        }

        int exactIndex = candidate.indexOf(previous);
        if (exactIndex >= 0) {
            return candidate.substring(exactIndex + previous.length()).trim();
        }

        if (candidateNormalized.startsWith(previousNormalized) || candidateNormalized.contains(previousNormalized)) {
            return "";
        }

        return candidate;
    }

    private static String normalizeNarrativeKey(String value) {
        return String.valueOf(value == null ? "" : value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\p{Punct}+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String sanitizeChatMessage(String text) {
        String sanitized = String.valueOf(text == null ? "" : text).trim();
        if (sanitized.isBlank()) {
            return CANVAS_UPDATE_MESSAGE;
        }

        sanitized = sanitized.replaceAll("\\s{2,}", " ").trim();
        return sanitized.length() > 500 ? sanitized.substring(0, 500).trim() : sanitized;
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





