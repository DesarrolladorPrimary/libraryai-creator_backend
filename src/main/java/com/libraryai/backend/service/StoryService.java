package com.libraryai.backend.service;

import java.time.LocalDateTime;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.StoryDao;
import com.libraryai.backend.models.Story;

/**
 * Servicio para la lógica de negocio de relatos.
 */
public class StoryService {

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
        
        // Validaciones básicas
        if (titulo == null || titulo.trim().isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El título es obligatorio");
            response.addProperty("status", 400);
            return response;
        }
        
        if (modoOrigen == null || modoOrigen.trim().isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El modo de origen es obligatorio");
            response.addProperty("status", 400);
            return response;
        }
        
        // Validar que el modo de origen sea válido
        if (!modoOrigen.equals("Seccion_Artificial") && !modoOrigen.equals("Seccion_Creativa")) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "Modo de origen inválido. Debe ser 'Seccion_Artificial' o 'Seccion_Creativa'");
            response.addProperty("status", 400);
            return response;
        }
        
        // Validar longitud del título
        if (titulo.length() > 255) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El título no puede exceder 255 caracteres");
            response.addProperty("status", 400);
            return response;
        }
        
        // Crear el objeto Story
        LocalDateTime ahora = LocalDateTime.now();
        Story story = new Story(
            0, // ID se genera en la base de datos
            usuarioId,
            estanteriaId,
            modeloUsadoId,
            titulo.trim(),
            modoOrigen,
            descripcion != null ? descripcion.trim() : "",
            ahora,
            ahora
        );
        
        // Guardar en la base de datos
        return StoryDao.create(story);
    }

    /**
     * Obtiene todos los relatos de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return JsonObject con la lista de relatos
     */
    public static JsonObject getUserStories(int usuarioId) {
        if (usuarioId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "ID de usuario inválido");
            response.addProperty("status", 400);
            return response;
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
        if (relatoId <= 0 || usuarioId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "IDs inválidos");
            response.addProperty("status", 400);
            return response;
        }
        
        JsonObject result = StoryDao.findById(relatoId);
        
        // Validar que el relato pertenezca al usuario
        if (result.get("status").getAsInt() == 200) {
            JsonObject relato = result.getAsJsonObject("relato");
            int relatoUsuarioId = relato.get("usuarioId").getAsInt();
            
            if (relatoUsuarioId != usuarioId) {
                JsonObject response = new JsonObject();
                response.addProperty("Mensaje", "No tienes permisos para acceder a este relato");
                response.addProperty("status", 403);
                return response;
            }
        }
        
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
                                        Integer estanteriaId, Integer modeloUsadoId) {
        
        // Validar IDs
        if (relatoId <= 0 || usuarioId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "IDs inválidos");
            response.addProperty("status", 400);
            return response;
        }
        
        // Verificar que el relato exista y pertenezca al usuario
        JsonObject existingStory = StoryDao.findById(relatoId);
        if (existingStory.get("status").getAsInt() != 200) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El relato no existe");
            response.addProperty("status", 404);
            return response;
        }
        
        JsonObject relatoData = existingStory.getAsJsonObject("relato");
        int relatoUsuarioId = relatoData.get("usuarioId").getAsInt();
        
        if (relatoUsuarioId != usuarioId) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "No tienes permisos para modificar este relato");
            response.addProperty("status", 403);
            return response;
        }
        
        // Validar modo de origen si se proporciona
        if (modoOrigen != null && !modoOrigen.trim().isEmpty()) {
            if (!modoOrigen.equals("Seccion_Artificial") && !modoOrigen.equals("Seccion_Creativa")) {
                JsonObject response = new JsonObject();
                response.addProperty("Mensaje", "Modo de origen inválido. Debe ser 'Seccion_Artificial' o 'Seccion_Creativa'");
                response.addProperty("status", 400);
                return response;
            }
        }
        
        // Validar título si se proporciona
        if (titulo != null && titulo.length() > 255) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El título no puede exceder 255 caracteres");
            response.addProperty("status", 400);
            return response;
        }
        
        // Crear objeto Story con los datos actualizados
        Story story = new Story(
            relatoId,
            usuarioId,
            estanteriaId,
            modeloUsadoId,
            titulo != null ? titulo.trim() : relatoData.get("titulo").getAsString(),
            modoOrigen != null ? modoOrigen.trim() : relatoData.get("modoOrigen").getAsString(),
            descripcion != null ? descripcion.trim() : relatoData.get("descripcion").getAsString(),
            LocalDateTime.parse(relatoData.get("fechaCreacion").getAsString().replace(" ", "T")),
            LocalDateTime.now() // Actualizar fecha de modificación
        );
        
        return StoryDao.update(story);
    }

    /**
     * Elimina un relato existente.
     * 
     * @param relatoId ID del relato a eliminar
     * @param usuarioId ID del usuario solicitante
     * @return JsonObject con el resultado de la operación
     */
    public static JsonObject deleteStory(int relatoId, int usuarioId) {
        if (relatoId <= 0 || usuarioId <= 0) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "IDs inválidos");
            response.addProperty("status", 400);
            return response;
        }
        
        // Verificar que el relato exista y pertenezca al usuario
        JsonObject existingStory = StoryDao.findById(relatoId);
        if (existingStory.get("status").getAsInt() != 200) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "El relato no existe");
            response.addProperty("status", 404);
            return response;
        }
        
        JsonObject relatoData = existingStory.getAsJsonObject("relato");
        int relatoUsuarioId = relatoData.get("usuarioId").getAsInt();
        
        if (relatoUsuarioId != usuarioId) {
            JsonObject response = new JsonObject();
            response.addProperty("Mensaje", "No tienes permisos para eliminar este relato");
            response.addProperty("status", 403);
            return response;
        }
        
        // TODO: Antes de eliminar, deberíamos eliminar los mensajes de chat asociados
        // Esto se implementará cuando tengamos el servicio de chat completo
        
        return StoryDao.delete(relatoId);
    }

    /**
     * Obtiene estadísticas de relatos de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return JsonObject con estadísticas
     */
    public static JsonObject getUserStoryStats(int usuarioId) {
        JsonObject response = new JsonObject();
        
        if (usuarioId <= 0) {
            response.addProperty("Mensaje", "ID de usuario inválido");
            response.addProperty("status", 400);
            return response;
        }
        
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
}
