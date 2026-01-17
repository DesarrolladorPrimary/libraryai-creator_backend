package com.libraryai.backend.util;

import com.google.gson.JsonObject;

public class QueryParams {

    public static JsonObject parseId(String idBody){
        
        JsonObject json = new JsonObject();
        String query = idBody.toString();
        
        if (query.isEmpty() || !query.matches("id=\\d+")) {
            json.addProperty("Mensaje", "query no valida o vacia");
            json.addProperty("status", 404);
            return json;
        }

        String[] parts = query.split("=");
        int id = Integer.parseInt(parts[parts.length - 1]);

        json.addProperty("id", id);
        json.addProperty("status", 200);


        return json;

    }
    
}