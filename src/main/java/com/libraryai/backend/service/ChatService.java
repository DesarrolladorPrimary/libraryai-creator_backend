package com.libraryai.backend.service;

import java.time.LocalDateTime;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.chats.ChatDao;

/**
 * Servicio para la lógica de negocio de chat.
 */
public class ChatService {

    /**
     * Envía un mensaje en el chat de un relato.
     * 
     * @param relatoId ID del relato
     * @param usuarioId ID del usuario que envía el mensaje
     * @param emisor Tipo de emisor ('Usuario', 'Poly', 'Sistema')
     * @param contenido Contenido del mensaje
     * @return JsonObject con el resultado de la operación
     */
    public static JsonObject sendMessage(int relatoId, int usuarioId, String emisor, String contenido) {
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
        
        // TODO: Validar que el usuario tenga permisos para enviar mensajes en este relato
        
        try {
            // Obtener el siguiente orden para el mensaje
            int siguienteOrden = getSiguienteOrden(relatoId);
            
            // Guardar el mensaje en la base de datos
            ChatDao.save(relatoId, emisor, contenido.trim(), siguienteOrden);
            
            // Si el mensaje es del usuario, generar respuesta de Poly
            if (emisor.equals("Usuario")) {
                generarRespuestaPoly(relatoId, contenido);
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
        
        // TODO: Validar que el usuario tenga permisos para ver este relato
        
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
    private static void generarRespuestaPoly(int relatoId, String mensajeUsuario) {
        try {
            // TODO: Implementar la lógica real con Gemini API
            // Por ahora, generamos una respuesta simple
            
            String respuestaPoly = generarRespuestaSimple(mensajeUsuario);
            
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

    /**
     * Genera una respuesta simple basada en el mensaje del usuario.
     * TODO: Reemplazar con la integración real a Gemini API
     * 
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
        
        // TODO: Validar que el usuario tenga permisos para limpiar este relato
        // TODO: Implementar método en ChatDao para eliminar mensajes por relato
        
        JsonObject response = new JsonObject();
        response.addProperty("Mensaje", "Funcionalidad no implementada aún");
        response.addProperty("status", 501);
        return response;
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
}
