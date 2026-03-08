package com.libraryai.backend.service;

import java.time.LocalDateTime;

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
            String respuestaPoly = generarRespuestaSimple(mensajeUsuario);
            String prompt = buildGeminiPrompt(relatoId, mensajeUsuario, archivoContexto);
            String instrucciones = buildGeminiInstructions(relatoId, usuarioId, parametrosIA);
            JsonObject geminiResponse = GeminiService.generateText(prompt, instrucciones);

            if (geminiResponse.has("status") && geminiResponse.get("status").getAsInt() == 200
                    && geminiResponse.has("AI")) {
                respuestaPoly = geminiResponse.get("AI").getAsString();
            }
            
            // Obtener el siguiente orden para la respuesta
            int siguienteOrden = getSiguienteOrden(relatoId);
            
            // Guardar la respuesta de Poly
            ChatDao.save(relatoId, "Poly", respuestaPoly, siguienteOrden);
            
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

        JsonObject filesResponse = UploadedFileDao.listByStory(relatoId);
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
        instrucciones.append("Entrega texto claro, coherente y util para ser mostrado en CanvasAI. ");
        instrucciones.append("Evita meta-explicaciones innecesarias y prioriza contenido narrativo cuando el usuario ya haya dado suficiente contexto.");

        JsonObject effectiveSettings = new JsonObject();
        JsonObject savedSettings = AIConfigurationDao.findByStoryId(relatoId);
        if (savedSettings.has("status") && savedSettings.get("status").getAsInt() == 200
                && savedSettings.has("configuracionIA")) {
            JsonObject storedConfig = savedSettings.getAsJsonObject("configuracionIA");
            for (String key : new String[] { "estiloEscritura", "nivelCreatividad", "longitudRespuesta", "tonoEmocional" }) {
                if (storedConfig.has(key)) {
                    effectiveSettings.add(key, storedConfig.get(key));
                }
            }
        }

        if (parametrosIA != null) {
            for (String key : new String[] { "estiloEscritura", "nivelCreatividad", "longitudRespuesta", "tonoEmocional" }) {
                if (parametrosIA.has(key)) {
                    effectiveSettings.add(key, parametrosIA.get(key));
                }
            }
        }

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

        JsonObject userInstructions = SettingsDao.getInstruccionIA(usuarioId);
        if (userInstructions.has("status") && userInstructions.get("status").getAsInt() == 200
                && userInstructions.has("instruccion")) {
            String instruccion = userInstructions.get("instruccion").getAsString().trim();
            if (!instruccion.isBlank()) {
                instrucciones.append("\n\nInstruccion permanente del usuario:\n").append(instruccion);
            }
        }

        return instrucciones.toString();
    }

    /**
     * Genera una respuesta simple basada en el mensaje del usuario.
     * @param mensajeUsuario Mensaje del usuario
     * @return Respuesta generada
     */
    private static String generarRespuestaSimple(String mensajeUsuario) {
        // Respuestas simples basadas en palabras clave
        String mensajeLower = mensajeUsuario.toLowerCase();
        
        if (mensajeLower.contains("hola") || mensajeLower.contains("buenos días")) {
            return "¡Hola! Soy Poly, tu asistente de escritura. ¿En qué puedo ayudarte con tu relato hoy?";
        }
        
        if (mensajeLower.contains("ayuda") || mensajeLower.contains("ayúdame")) {
            return "Claro que sí. Puedo ayudarte a desarrollar personajes, crear diálogos, describir escenas o sugerir giros argumentales. ¿Qué te gustaría explorar?";
        }
        
        if (mensajeLower.contains("personaje")) {
            return "Para crear un personaje memorable, considera sus motivaciones, miedos, y cómo cambia a lo largo de la historia. ¿Qué tipo de personaje tienes en mente?";
        }
        
        if (mensajeLower.contains("diálogo") || mensajeLower.contains("conversación")) {
            return "Un buen diálogo debe revelar carácter y avanzar la trama. Piensa en lo que cada personaje quiere decir vs lo que realmente necesita decir. ¿En qué escena necesitas ayuda?";
        }
        
        if (mensajeLower.contains("escena") || mensajeLower.contains("ambientación")) {
            return "Una escena efectiva debe tener un propósito claro, conflicto y emociones. Describe el entorno a través de los sentidos y las reacciones de los personajes. ¿Qué tipo de escena estás escribiendo?";
        }
        
        if (mensajeLower.contains("final") || mensajeLower.contains("conclusión")) {
            return "Un buen final debe resolver las tramas principales mientras deja una impresión duradera. Considera si quieres un final cerrado, abierto o ambiguo. ¿Hacia dónde se dirige tu historia?";
        }
        
        // Respuesta por defecto
        return "Entiendo tu mensaje. Como tu asistente de escritura, puedo ayudarte a desarrollar ideas, personajes, diálogos o cualquier aspecto de tu relato. ¿Qué te gustaría explorar específicamente?";
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
