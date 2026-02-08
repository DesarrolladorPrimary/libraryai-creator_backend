package com.libraryai.backend.service.shelves;

import com.google.gson.JsonObject;
import com.libraryai.backend.dao.shelves.ShelfDao;

public class ShelfService {
        public static JsonObject createShelf(int idUser, String nombreCategoria){

            JsonObject response = new JsonObject();


            if (nombreCategoria.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ]+") && nombreCategoria.length() > 3) {
                response = ShelfDao.createShelf(idUser, nombreCategoria);
            }
            else
            {
                response.addProperty("Mensaje", "El nombre no es correcto");
                response.addProperty("Status", 400);
            }

            return response;
        }
}
