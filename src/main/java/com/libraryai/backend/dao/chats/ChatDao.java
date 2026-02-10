package com.libraryai.backend.dao.chats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.libraryai.backend.config.ConexionDB;

public class ChatDao {
    // language=sql;
    static String SQL_INSERT = """
            INSERT INTO MensajeChat (FK_RelatoID, Emisor, ContenidoMensaje, Orden) VALUES (?, ?, ?, ?)
            """;;

    public static void guardar(int idRelato, String emisor, String mensaje, int orden){
        try (
            Connection conn = ConexionDB.getConexion();
            PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT);
    ) {
        pstmt.setInt(0, idRelato );
        pstmt.setString(1, emisor);
        pstmt.setString(2, mensaje);
        pstmt.setInt(3, orden);
        int filasAfectadas = pstmt.executeUpdate();

        if (filasAfectadas > 0) {
            System.out.println("Mensaje recibido correctamente");
        }

            
        } catch (SQLException e) {
            System.err.println("Error al crear o recibir los mensajes. E: "+ e.getMessage());
        }

    }

    public static void listarPorRelato(){

    }
}
