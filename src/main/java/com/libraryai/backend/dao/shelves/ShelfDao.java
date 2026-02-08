package com.libraryai.backend.dao.shelves;

import java.sql.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.DbConnection;

public class ShelfDao {
        
        // language=sql
        private static String SQL_INSERT = """
                        INSERT INTO Estanteria(FK_UsuarioID, NombreCategoria) VALUES(?,?);
                        """;

        // language=sql
        private static String SQL_SELECT = """
                        SELECT * FROM Estanteria WHERE FK_UsuarioID = ?;
                        """;

        // language=sql
        private static String SQL_UPDATE = """
                        UPDATE
                        Estanteria
                         SET
                        NombreCategoria = ?

                         WHERE
                        PK_EstanteriaID = ?
                        """;

        // language=sql
        private static String SQL_DELETE = """
                        DELETE FROM  Estanteria WHERE PK_EstanteriaID = ?;
                        """;

        public static JsonObject createShelf(int idUser, String nombreEstanteria) {
                JsonObject responseJson = new JsonObject();
                try (
                                Connection conn = DbConnection.getConnection();
                                PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);) {
                        pstmt.setInt(1, idUser);
                        pstmt.setString(2, nombreEstanteria);

                        int filasAfect = pstmt.executeUpdate();

                        try (ResultSet generateKeys = pstmt.getGeneratedKeys()) {
                                if (generateKeys.next()) {
                                        responseJson.addProperty("Estanteria_id", generateKeys.getInt(1));
                                }
                        }
                        if (filasAfect > 0) {
                                responseJson.addProperty("Status", 201);
                        }

                        return responseJson;
                } catch (SQLException e) {
                        e.printStackTrace();
                        responseJson.addProperty("Status", 500);
                        responseJson.addProperty("Mensaje", "Error al crear estantería: " + e.getMessage());
                        return responseJson;
                }
        }

        public static JsonArray getShelvesByUserId(int idUser) {
    JsonArray estanteriasArray = new JsonArray();
    
    try (Connection conn = DbConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT)) {
        
        pstmt.setInt(1, idUser);
        ResultSet rs = pstmt.executeQuery();
        
        if (!rs.next()) {
            JsonObject vacioResponse = new JsonObject();
            vacioResponse.addProperty("Mensaje", "No hay estanterías creadas");
            vacioResponse.addProperty("Status", 200);  // 200 = OK
            estanteriasArray.add(vacioResponse);
            return estanteriasArray;
        }
        
        do {
            JsonObject estanteriaJson = new JsonObject();
            estanteriaJson.addProperty("PK_EstanteriaID", rs.getInt("PK_EstanteriaID"));
            estanteriaJson.addProperty("NombreCategoria", rs.getString("NombreCategoria"));
            estanteriasArray.add(estanteriaJson);
        } while (rs.next());
        
    } catch (SQLException e) {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("Mensaje", "Error: " + e.getMessage());
        errorResponse.addProperty("Status", 500);
        estanteriasArray.add(errorResponse);
    }
    
    return estanteriasArray;

        }



        public static JsonObject updateShelf(String nombreCategoria, int idEstanteria){
                JsonObject response = new JsonObject();
                try (
                        Connection conn = DbConnection.getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE);
                ) {
                        pstmt.setString(1, nombreCategoria);
                        pstmt.setInt(2, idEstanteria);

                        int filasAfect = pstmt.executeUpdate();

                        if (filasAfect > 0) {
                                response.addProperty("Mensaje", "Actualizado correctamente");
                                response.addProperty("Status", 200);
                        }
                        else{
                                response.addProperty("Mensaje", "No se pudo actualizar la estanteria");
                                response.addProperty("Status", 404);
                        }

                        return response;
                        
                        
                
                } catch (SQLException e) {
                        e.printStackTrace();

                        response.addProperty("Mensaje", "Se produjo un error al actualizar el nombre de la estanteria. " + e.getMessage());
                        response.addProperty("Status", 500);
                        return response;
                }
        }



        public static JsonObject deleteShelf(int idEstanteria){
                JsonObject response = new JsonObject();
                try (
                        Connection conn = DbConnection.getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE);
                ) {
                        pstmt.setInt(1, idEstanteria);

                        int filasAfect = pstmt.executeUpdate();

                        if (filasAfect > 0) {
                                response.addProperty("Mensaje", "Se elimino correctamente");
                                response.addProperty("Status", 200);
                        }
                        else{
                                response.addProperty("Mensaje", "No se pudo eliminar la estanteria");
                                response.addProperty("Status", 404);
                        }

                        return response;
                        
                        
                
                } catch (SQLException e) {
                        e.printStackTrace();

                        response.addProperty("Mensaje", "Se produjo un error al eliminar la estanteria. " + e.getMessage());
                        response.addProperty("Status", 500);
                        return response;
                }
        }


        

}