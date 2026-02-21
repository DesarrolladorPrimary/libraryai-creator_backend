package com.libraryai.backend.dao.chats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;

public class ChatDao {
    // language=sql;
    static String SQL_INSERT = """
            INSERT INTO MensajeChat (FK_RelatoID, Emisor, ContenidoMensaje, Orden) VALUES (?, ?, ?, ?)
            """;;
    
    static String SQL_SELECT_BY_RELATO = """
            SELECT PK_MensajeID, Emisor, ContenidoMensaje, Orden, FechaEnvio 
            FROM MensajeChat 
            WHERE FK_RelatoID = ? 
            ORDER BY Orden ASC
            """;

    public static void save(int idRelato, String emisor, String mensaje, int orden){
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT);
        ) {
            pstmt.setInt(1, idRelato);
            pstmt.setString(2, emisor);
            pstmt.setString(3, mensaje);
            pstmt.setInt(4, orden);
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Mensaje recibido correctamente");
            }

        } catch (SQLException e) {
            System.err.println("Error al crear o recibir los mensajes. E: "+ e.getMessage());
        }

    }

    public static JsonObject listByStory(int idRelato) {
        JsonObject response = new JsonObject();
        JsonArray mensajes = new JsonArray();
        
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_RELATO);
        ) {
            pstmt.setInt(1, idRelato);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                JsonObject mensaje = new JsonObject();
                mensaje.addProperty("id", rs.getInt("PK_MensajeID"));
                mensaje.addProperty("emisor", rs.getString("Emisor"));
                mensaje.addProperty("contenido", rs.getString("ContenidoMensaje"));
                mensaje.addProperty("orden", rs.getInt("Orden"));
                mensaje.addProperty("fecha", rs.getTimestamp("FechaEnvio").toString());
                mensajes.add(mensaje);
            }
            
            response.add("mensajes", mensajes);
            response.addProperty("status", 200);
            
        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al obtener mensajes: " + e.getMessage());
            response.addProperty("status", 500);
        }
        
        return response;
    }
}
