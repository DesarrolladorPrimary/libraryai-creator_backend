package com.libraryai.backend.dao.moderation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO de soporte para moderación.
 *
 * <p>Lee la blacklist persistida y registra eventos de contenido bloqueado sin
 * afectar el flujo principal de negocio si falla la auditoría.
 */
public class ModerationDao {

    private static final String SQL_SELECT_FORBIDDEN_WORDS = """
            SELECT Palabra
            FROM PalabraProhibida
            ORDER BY Palabra ASC
            """;

    private static final String SQL_INSERT_LOG = """
            INSERT INTO LogModeracion (FK_UsuarioID, Motivo, ContenidoBloqueadoHash)
            VALUES (?, ?, ?)
            """;

    /**
     * Devuelve la blacklist completa ordenada alfabéticamente.
     */
    public static List<String> listForbiddenWords() {
        List<String> words = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_FORBIDDEN_WORDS);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String word = rs.getString("Palabra");
                if (word != null && !word.isBlank()) {
                    words.add(word.trim());
                }
            }
        } catch (Exception e) {
            return words;
        }

        return words;
    }

    /**
     * Registra un evento de moderación para trazabilidad operativa.
     *
     * <p>No obliga a asociar una palabra exacta porque algunos bloqueos vienen
     * de heurísticas o patrones más amplios que una coincidencia literal de la
     * blacklist.
     */
    public static void createModerationLog(int userId, String reason, String blockedContentHash) {
        if (userId <= 0 || reason == null || reason.isBlank()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_LOG)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, reason);
            pstmt.setString(3, blockedContentHash);
            pstmt.executeUpdate();
        } catch (Exception e) {
            // La moderación no debe romper el flujo por un fallo de auditoría.
        }
    }
}
