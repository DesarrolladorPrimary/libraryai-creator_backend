package com.libraryai.backend.util;

import com.google.gson.JsonObject;

/**
 * Parser de query params.
 */
public class QueryParams {

    /**
     * Extrae el parametro id desde una query tipo "id=123".
     * Retorna un JsonObject con:
     * - id: valor parseado
     * - status: 200 si es valido, 404 si es invalido o vacio
     * - Mensaje: descripcion del error cuando aplica
     */
    public static JsonObject parseId(String idBody){
        
        JsonObject json = new JsonObject();
        // Normaliza a String por si viene un objeto o null.
        String query = idBody.toString();
        
        // Valida formato simple "id=numero".
        if (query.isEmpty() || !query.matches("id=\\d+")) {
            json.addProperty("Mensaje", "query no valida o vacia");
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
