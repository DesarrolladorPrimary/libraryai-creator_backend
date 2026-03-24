package com.libraryai.backend.dao.chats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO para persistir y consultar el historial de mensajes asociado a un relato.
 *
 * <p>Trabaja con un orden secuencial por relato. Si la inserción colisiona con
 * la restricción única de {@code (FK_RelatoID, Orden)}, recalcula el siguiente
 * orden y reintenta una vez.
 */
public class ChatDao {
    // language=sql;
    static String SQL_INSERT = """
            INSERT INTO MensajeChat (FK_RelatoID, Emisor, ContenidoMensaje, Orden) VALUES (?, ?, ?, ?)
            """;;

    static String SQL_NEXT_ORDER = """
            SELECT COALESCE(MAX(Orden), 0) + 1 AS SiguienteOrden
            FROM MensajeChat
            WHERE FK_RelatoID = ?
            """;
    
    static String SQL_SELECT_BY_RELATO = """
            SELECT PK_MensajeID, Emisor, ContenidoMensaje, Orden, FechaEnvio 
            FROM MensajeChat 
            WHERE FK_RelatoID = ? 
            ORDER BY Orden ASC
            """;

    static String SQL_DELETE_BY_RELATO = """
            DELETE FROM MensajeChat
            WHERE FK_RelatoID = ?
            """;

    /**
     * Guarda un mensaje dentro del historial del chat del relato.
     */
    public static void save(int idRelato, String emisor, String mensaje, int orden){
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (insertMessage(conn, idRelato, emisor, mensaje, orden)) {
                System.out.println("Mensaje recibido correctamente");
                return;
            }

            int retryOrder = getNextOrder(conn, idRelato);
            if (insertMessage(conn, idRelato, emisor, mensaje, retryOrder)) {
                System.out.println("Mensaje recibido correctamente");
            }
        } catch (SQLException e) {
            System.err.println("Error al crear o recibir los mensajes. E: "+ e.getMessage());
        }

    }

    /**
     * Intenta persistir el mensaje con el orden recibido.
     *
     * @return {@code false} solo cuando detecta una colisión recuperable de orden.
     */
    private static boolean insertMessage(Connection conn, int idRelato, String emisor, String mensaje, int orden)
            throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
            pstmt.setInt(1, idRelato);
            pstmt.setString(2, emisor);
            pstmt.setString(3, mensaje);
            pstmt.setInt(4, orden);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isDuplicateOrderError(e)) {
                return false;
            }

            throw e;
        }
    }

    /**
     * Calcula el siguiente orden disponible dentro del historial del relato.
     */
    private static int getNextOrder(Connection conn, int idRelato) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_NEXT_ORDER)) {
            pstmt.setInt(1, idRelato);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("SiguienteOrden");
            }
        }

        return 1;
    }

    /**
     * Detecta si el error SQL corresponde a la unicidad del orden por relato.
     */
    private static boolean isDuplicateOrderError(SQLException exception) {
        String sqlState = exception.getSQLState();
        String message = exception.getMessage();

        if (!"23000".equals(sqlState) && !"23505".equals(sqlState)) {
            return false;
        }

        if (message == null) {
            return true;
        }

        String normalizedMessage = message.toLowerCase();
        return normalizedMessage.contains("duplicate")
                && normalizedMessage.contains("orden");
    }

    /**
     * Devuelve el historial completo del chat ordenado por secuencia de mensaje.
     */
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

    /**
     * Elimina todo el historial de chat vinculado a un relato.
     */
    public static JsonObject deleteByStory(int idRelato) {
        JsonObject response = new JsonObject();

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_BY_RELATO);
        ) {
            pstmt.setInt(1, idRelato);
            int deletedRows = pstmt.executeUpdate();

            response.addProperty("Mensaje", deletedRows > 0
                    ? "Historial eliminado correctamente"
                    : "No había mensajes para eliminar");
            response.addProperty("filasAfectadas", deletedRows);
            response.addProperty("status", 200);

        } catch (SQLException e) {
            response.addProperty("Mensaje", "Error al eliminar mensajes: " + e.getMessage());
            response.addProperty("status", 500);
        }

        return response;
    }
}
