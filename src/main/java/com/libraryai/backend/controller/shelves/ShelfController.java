package com.libraryai.backend.controller.shelves;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.shelves.ShelfService;
import com.sun.net.httpserver.HttpHandler;

public class ShelfController {

    public static HttpHandler createShelf(){
        return exchange ->  {
            ApiRequest request = new ApiRequest(exchange);
            JsonObject datosJson = new JsonObject();
            Gson gson = new Gson();


            String body = request.readBody();


            datosJson = gson.fromJson(body, JsonObject.class);


            int id = datosJson.get("id").getAsInt();
            String nombreCategoria = datosJson.get("nameCategory").getAsString(); 

            JsonObject responseJson = ShelfService.createShelf(id, nombreCategoria);

            
            int code = 0;
            if (responseJson.has("status")) {
                code = responseJson.get("status").getAsInt();
            }

            String response = responseJson.toString();

            ApiResponse.send(exchange, response, code);





        };
    }
    
}
