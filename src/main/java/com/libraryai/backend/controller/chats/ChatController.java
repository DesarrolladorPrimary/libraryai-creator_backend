package com.libraryai.backend.controller.chats;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.chat.ChatService;
import com.libraryai.backend.util.JwtUtil;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador para operaciones de chat.
 */
public class ChatController {
    
    /**
     * Handler para enviar un mensaje al chat de un relato.
     * Espera un JSON con: relatoId, emisor, contenido
     */
    public static HttpHandler sendMessage() {
        return exchange -> {
            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();
            
            System.out.println("Petición POST /api/v1/chat/message recibida");
            
            try {
                // Parsear el JSON del body
                Gson gson = new Gson();
                JsonObject messageData = gson.fromJson(body, JsonObject.class);
                
                // Extraer datos del JSON
                int relatoId = messageData.get("relatoId").getAsInt();
                String emisor = messageData.get("emisor").getAsString();
                String contenido = messageData.get("contenido").getAsString();
                JsonObject parametrosIA = messageData.has("parametrosIA") && messageData.get("parametrosIA").isJsonObject()
                        ? messageData.getAsJsonObject("parametrosIA")
                        : null;
                JsonObject archivoContexto = messageData.has("archivoContexto")
                        && messageData.get("archivoContexto").isJsonObject()
                                ? messageData.getAsJsonObject("archivoContexto")
                                : null;
                
                // Obtener usuarioId del token JWT
                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }
                
                // Enviar mensaje
                JsonObject response = ChatService.sendMessage(
                        relatoId,
                        usuarioId,
                        emisor,
                        contenido,
                        parametrosIA,
                        archivoContexto);
                int statusCode = response.get("status").getAsInt();
                
                ApiResponse.send(exchange, response.toString(), statusCode);
                
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al procesar la petición: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }
    
    /**
     * Handler para obtener el historial de chat de un relato.
     * Espera el relatoId como parámetro en la URL: /api/v1/chat/{relatoId}
     */
    public static HttpHandler getChatHistory() {
        return exchange -> {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            
            System.out.println("Petición GET " + path + " recibida");
            
            try {
                // Extraer ID del relato de la URL
                if (pathParts.length < 5) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "ID de relato no proporcionado");
                    errorResponse.addProperty("status", 400);
                    ApiResponse.send(exchange, errorResponse.toString(), 400);
                    return;
                }
                
                int relatoId;
                try {
                    relatoId = Integer.parseInt(pathParts[4]);
                } catch (NumberFormatException e) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "ID de relato inválido");
                    errorResponse.addProperty("status", 400);
                    ApiResponse.send(exchange, errorResponse.toString(), 400);
                    return;
                }
                
                // Obtener usuarioId del token JWT
                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }
                
                // Obtener historial
                JsonObject response = ChatService.getChatHistory(relatoId, usuarioId);
                int statusCode = response.get("status").getAsInt();
                
                ApiResponse.send(exchange, response.toString(), statusCode);
                
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al obtener historial: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }
    
    /**
     * Handler para limpiar el historial de chat de un relato.
     * Espera el relatoId como parámetro en la URL: /api/v1/chat/{relatoId}/clear
     */
    public static HttpHandler clearChatHistory() {
        return exchange -> {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            
            System.out.println("Petición DELETE " + path + " recibida");
            
            try {
                // Extraer ID del relato de la URL
                if (pathParts.length < 6) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "ID de relato no proporcionado");
                    errorResponse.addProperty("status", 400);
                    ApiResponse.send(exchange, errorResponse.toString(), 400);
                    return;
                }
                
                int relatoId;
                try {
                    relatoId = Integer.parseInt(pathParts[4]);
                } catch (NumberFormatException e) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "ID de relato inválido");
                    errorResponse.addProperty("status", 400);
                    ApiResponse.send(exchange, errorResponse.toString(), 400);
                    return;
                }
                
                // Obtener usuarioId del token JWT
                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }
                
                // Limpiar historial
                JsonObject response = ChatService.clearChatHistory(relatoId, usuarioId);
                int statusCode = response.get("status").getAsInt();
                
                ApiResponse.send(exchange, response.toString(), statusCode);
                
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al limpiar historial: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }
    
    /**
     * Handler para obtener estadísticas del chat de un relato.
     * Espera el relatoId como parámetro en la URL: /api/v1/chat/{relatoId}/stats
     */
    public static HttpHandler getChatStats() {
        return exchange -> {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            
            System.out.println("Petición GET " + path + " recibida");
            
            try {
                // Extraer ID del relato de la URL
                if (pathParts.length < 6) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "ID de relato no proporcionado");
                    errorResponse.addProperty("status", 400);
                    ApiResponse.send(exchange, errorResponse.toString(), 400);
                    return;
                }
                
                int relatoId;
                try {
                    relatoId = Integer.parseInt(pathParts[4]);
                } catch (NumberFormatException e) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "ID de relato inválido");
                    errorResponse.addProperty("status", 400);
                    ApiResponse.send(exchange, errorResponse.toString(), 400);
                    return;
                }
                
                // Obtener usuarioId del token JWT
                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }
                
                // Obtener estadísticas
                JsonObject response = ChatService.getChatStats(relatoId, usuarioId);
                int statusCode = response.get("status").getAsInt();
                
                ApiResponse.send(exchange, response.toString(), statusCode);
                
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al obtener estadísticas: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
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
