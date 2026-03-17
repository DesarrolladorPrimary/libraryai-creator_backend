package com.libraryai.backend.util;

import com.google.gson.JsonObject;

/**
 * Utilidad mínima para parsear query params simples usados por el backend.
 *
 * <p>La clase existe para mantener consistencia en respuestas de error cuando la
 * aplicación espera identificadores por query string.
 */
public class QueryParams {

    /**
     * Extrae el parametro id desde una query tipo "id=123".
     *
     * <p>Retorna un JsonObject con:
     * - id: valor parseado
     * - status: 200 si es válido, 404 si es inválido o vacío
     * - Mensaje: descripcion del error cuando aplica
     */
    public static JsonObject parseId(String idBody){
        
        JsonObject json = new JsonObject();
        // Normaliza a String por si viene un objeto o null.
        String query = idBody.toString();
        
        // Valida formato simple "id=número".
        if (query.isEmpty() || !query.matches("id=\\d+")) {
            json.addProperty("Mensaje", "query no válida o vacía");
            json.addProperty("status", 404);
            return json;
        }

        // Separa el valor despues del '=' y lo convierte a entero.
        String[] parts = query.split("=");
        int id = Integer.parseInt(parts[parts.length - 1]);

        json.addProperty("id", id);
        json.addProperty("status", 200);


        return json;

    }
    
}
