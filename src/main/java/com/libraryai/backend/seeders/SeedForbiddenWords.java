package com.libraryai.backend.seeders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.libraryai.backend.config.DatabaseConnection;

/**
 * Inserta una blacklist mínima para moderación básica.
 */
public class SeedForbiddenWords {

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM PalabraProhibida";

    private static final String SQL_INSERT = "INSERT INTO PalabraProhibida(Palabra) VALUES(?)";

    private static final String[] DEFAULT_WORDS = new String[] {
            "pornografia",
            "sexo explicito",
            "violacion",
            "incesto",
            "zoofilia",
            "pedofilia"
    };

    public static void insertForbiddenWords() {
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement countStmt = conn.prepareStatement(SQL_COUNT);
                ResultSet rs = countStmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("La lista de palabras prohibidas ya existe en la base de datos");
                return;
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(SQL_INSERT)) {
                int inserted = 0;
                for (String word : DEFAULT_WORDS) {
                    insertStmt.setString(1, word);
                    inserted += insertStmt.executeUpdate();
                }
                System.out.println("Total de palabras prohibidas insertadas: " + inserted);
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar palabras prohibidas: " + e.getMessage());
        }
    }
}
