package com.libraryai.backend.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.StoryService;
import com.libraryai.backend.util.JwtUtil;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador para operaciones de relatos.
 */
public class StoryController {
    
    /**
     * Handler para crear un nuevo relato.
     * Espera un JSON con: titulo, modoOrigen, descripcion, estanteriaId, modeloUsadoId
     */
    public static HttpHandler createStory() {
        return exchange -> {
            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();
            
            System.out.println("Petición POST /api/v1/stories recibida");
            
            try {
                // Parsear el JSON del body
                Gson gson = new Gson();
                JsonObject storyData = gson.fromJson(body, JsonObject.class);
                
                // Extraer datos del JSON
                String titulo = storyData.has("titulo") ? storyData.get("titulo").getAsString() : null;
                String modoOrigen = storyData.has("modoOrigen") ? storyData.get("modoOrigen").getAsString() : null;
                String descripcion = storyData.has("descripcion") ? storyData.get("descripcion").getAsString() : null;
                Integer estanteriaId = storyData.has("estanteriaId") ? storyData.get("estanteriaId").getAsInt() : null;
                Integer modeloUsadoId = storyData.has("modeloUsadoId") ? storyData.get("modeloUsadoId").getAsInt() : null;
                
                // Obtener usuarioId del token JWT (del header Authorization)
                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }
                
                // Crear el relato
                JsonObject response = StoryService.createStory(usuarioId, titulo, modoOrigen, descripcion, estanteriaId, modeloUsadoId);
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
     * Handler para obtener todos los relatos de un usuario.
     */
    public static HttpHandler getUserStories() {
        return exchange -> {
            System.out.println("Petición GET /api/v1/stories recibida");
            
            try {
                // Obtener usuarioId del token JWT
                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }
                
                // Obtener relatos del usuario
                JsonObject response = StoryService.getUserStories(usuarioId);
                int statusCode = response.get("status").getAsInt();
                
                ApiResponse.send(exchange, response.toString(), statusCode);
                
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al obtener relatos: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }
    
    /**
     * Handler para obtener un relato específico por ID.
     * Espera el ID como parámetro en la URL: /api/v1/stories/{id}
     */
    public static HttpHandler getStoryById() {
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
                
                // Obtener relato
                JsonObject response = StoryService.getStoryById(relatoId, usuarioId);
                int statusCode = response.get("status").getAsInt();
                
                ApiResponse.send(exchange, response.toString(), statusCode);
                
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al obtener relato: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }
    
    /**
     * Handler para actualizar un relato existente.
     * Espera el ID en la URL y los datos en el body.
     */
    public static HttpHandler updateStory() {
        return exchange -> {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            
            System.out.println("Petición PUT " + path + " recibida");
            
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
                
                // Parsear el JSON del body
                ApiRequest request = new ApiRequest(exchange);
                String body = request.readBody();
                Gson gson = new Gson();
                JsonObject storyData = gson.fromJson(body, JsonObject.class);
                
                // Extraer datos del JSON (todos opcionales para actualización parcial)
                String titulo = storyData.has("titulo") ? storyData.get("titulo").getAsString() : null;
                String modoOrigen = storyData.has("modoOrigen") ? storyData.get("modoOrigen").getAsString() : null;
                String descripcion = storyData.has("descripcion") ? storyData.get("descripcion").getAsString() : null;
                boolean hasShelfField = storyData.has("estanteriaId");
                Integer estanteriaId = hasShelfField && !storyData.get("estanteriaId").isJsonNull() ? 
                    storyData.get("estanteriaId").getAsInt() : null;
                Integer modeloUsadoId = storyData.has("modeloUsadoId") && !storyData.get("modeloUsadoId").isJsonNull() ? 
                    storyData.get("modeloUsadoId").getAsInt() : null;
                
                // Obtener usuarioId del token JWT
                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }
                
                // Actualizar relato
                JsonObject response = StoryService.updateStory(relatoId, usuarioId, titulo, modoOrigen, 
                    descripcion, estanteriaId, modeloUsadoId, hasShelfField);
                int statusCode = response.get("status").getAsInt();
                
                ApiResponse.send(exchange, response.toString(), statusCode);
                
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al actualizar relato: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }
    
    /**
     * Handler para eliminar un relato.
     * Espera el ID como parámetro en la URL: /api/v1/stories/{id}
     */
    public static HttpHandler deleteStory() {
        return exchange -> {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            
            System.out.println("Petición DELETE " + path + " recibida");
            
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
                
                // Eliminar relato
                JsonObject response = StoryService.deleteStory(relatoId, usuarioId);
                int statusCode = response.get("status").getAsInt();
                
                ApiResponse.send(exchange, response.toString(), statusCode);
                
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al eliminar relato: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }

    /**
     * Handler para obtener la configuración de IA de un relato.
     * Ruta: GET /api/v1/stories/{id}/configuracion-ia
     */
    public static HttpHandler getAIConfiguration() {
        return exchange -> {
            String path = exchange.getRequestURI().getPath();

            try {
                int relatoId = extractStoryId(path, 6);
                if (relatoId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "ID de relato inválido");
                    errorResponse.addProperty("status", 400);
                    ApiResponse.send(exchange, errorResponse.toString(), 400);
                    return;
                }

                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }

                JsonObject response = StoryService.getAIConfiguration(relatoId, usuarioId);
                ApiResponse.send(exchange, response.toString(), response.get("status").getAsInt());

            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al obtener configuración IA: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }

    /**
     * Handler para actualizar la configuración de IA de un relato.
     * Ruta: PUT /api/v1/stories/{id}/configuracion-ia
     */
    public static HttpHandler updateAIConfiguration() {
        return exchange -> {
            String path = exchange.getRequestURI().getPath();

            try {
                int relatoId = extractStoryId(path, 6);
                if (relatoId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "ID de relato inválido");
                    errorResponse.addProperty("status", 400);
                    ApiResponse.send(exchange, errorResponse.toString(), 400);
                    return;
                }

                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }

                ApiRequest request = new ApiRequest(exchange);
                String body = request.readBody();
                JsonObject payload = new Gson().fromJson(body, JsonObject.class);

                String writingStyle = payload != null && payload.has("estiloEscritura")
                        ? payload.get("estiloEscritura").getAsString()
                        : null;
                String creativityLevel = payload != null && payload.has("nivelCreatividad")
                        ? payload.get("nivelCreatividad").getAsString()
                        : null;
                String responseLength = payload != null && payload.has("longitudRespuesta")
                        ? payload.get("longitudRespuesta").getAsString()
                        : null;
                String emotionalTone = payload != null && payload.has("tonoEmocional")
                        ? payload.get("tonoEmocional").getAsString()
                        : null;

                JsonObject response = StoryService.updateAIConfiguration(relatoId, usuarioId, writingStyle,
                        creativityLevel, responseLength, emotionalTone);
                ApiResponse.send(exchange, response.toString(), response.get("status").getAsInt());

            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al actualizar configuración IA: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }

    /**
     * Handler para exportar un relato y guardar snapshot en DB.
     * Ruta: POST /api/v1/stories/{id}/export
     */
    public static HttpHandler exportStory() {
        return exchange -> {
            String path = exchange.getRequestURI().getPath();

            try {
                int relatoId = extractStoryId(path, 6);
                if (relatoId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "ID de relato inválido");
                    errorResponse.addProperty("status", 400);
                    ApiResponse.send(exchange, errorResponse.toString(), 400);
                    return;
                }

                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                if (usuarioId <= 0) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("Mensaje", "Usuario no autenticado");
                    errorResponse.addProperty("status", 401);
                    ApiResponse.send(exchange, errorResponse.toString(), 401);
                    return;
                }

                ApiRequest request = new ApiRequest(exchange);
                String body = request.readBody();
                JsonObject payload = new Gson().fromJson(body, JsonObject.class);

                String title = payload != null && payload.has("titulo")
                        ? payload.get("titulo").getAsString()
                        : null;
                String content = payload != null && payload.has("contenido")
                        ? payload.get("contenido").getAsString()
                        : null;
                String format = payload != null && payload.has("formato")
                        ? payload.get("formato").getAsString()
                        : null;
                boolean hasShelfField = payload != null && payload.has("estanteriaId");
                Integer shelfId = hasShelfField && !payload.get("estanteriaId").isJsonNull()
                        ? payload.get("estanteriaId").getAsInt()
                        : null;
                String fileName = payload != null && payload.has("nombreArchivo") && !payload.get("nombreArchivo").isJsonNull()
                        ? payload.get("nombreArchivo").getAsString()
                        : null;
                String fileType = payload != null && payload.has("tipoArchivo") && !payload.get("tipoArchivo").isJsonNull()
                        ? payload.get("tipoArchivo").getAsString()
                        : null;
                String fileBase64 = payload != null && payload.has("archivoBase64") && !payload.get("archivoBase64").isJsonNull()
                        ? payload.get("archivoBase64").getAsString()
                        : null;

                JsonObject response = StoryService.exportStory(relatoId, usuarioId, title, content, format,
                        shelfId, hasShelfField, fileName, fileType, fileBase64);
                ApiResponse.send(exchange, response.toString(), response.get("status").getAsInt());

            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("Mensaje", "Error al exportar relato: " + e.getMessage());
                errorResponse.addProperty("status", 500);
                ApiResponse.send(exchange, errorResponse.toString(), 500);
            }
        };
    }
    
    /**
     * Handler para obtener estadísticas de relatos del usuario.
     */
    public static HttpHandler getUserStoryStats() {
        return exchange -> {
            System.out.println("Petición GET /api/v1/stories/stats recibida");
            
            try {
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
                JsonObject response = StoryService.getUserStoryStats(usuarioId);
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

    private static int extractStoryId(String path, int minSegments) {
        String[] pathParts = path.split("/");
        if (pathParts.length < minSegments) {
            return -1;
        }

        try {
            return Integer.parseInt(pathParts[4]);
        } catch (NumberFormatException e) {
            return -1;
        }
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
