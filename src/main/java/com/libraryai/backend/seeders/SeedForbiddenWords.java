package com.libraryai.backend.seeders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.libraryai.backend.config.DatabaseConnection;

/**
 * Siembra la blacklist mínima usada por la moderación de texto.
 *
 * <p>El seeder es idempotente: solo inserta palabras faltantes para no duplicar
 * registros al reiniciar la aplicación.
 */
public class SeedForbiddenWords {

    private static final String SQL_EXISTS = "SELECT 1 FROM PalabraProhibida WHERE Palabra = ? LIMIT 1";

    private static final String SQL_INSERT = "INSERT INTO PalabraProhibida(Palabra) VALUES(?)";

    private static final String[] DEFAULT_WORDS = new String[] {
            "pornografia",
            "porno",
            "contenido adulto",
            "xxx",
            "sexo",
            "sexo explicito",
            "erotico",
            "erotica",
            "violacion",
            "incesto",
            "zoofilia",
            "pedofilia"
    };

    /**
     * Garantiza la presencia del vocabulario básico que bloquea contenido NSFW en
     * Poly, creativo y configuraciones IA.
     */
    public static void insertForbiddenWords() {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement existsStmt = conn.prepareStatement(SQL_EXISTS);
                PreparedStatement insertStmt = conn.prepareStatement(SQL_INSERT)) {

            int inserted = 0;
            for (String word : DEFAULT_WORDS) {
                // Cada palabra se consulta antes para que el seeder pueda correrse
                // varias veces sin crecer indefinidamente.
                existsStmt.setString(1, word);
                boolean exists;

                try (var rs = existsStmt.executeQuery()) {
                    exists = rs.next();
                }

                if (!exists) {
                    insertStmt.setString(1, word);
                    inserted += insertStmt.executeUpdate();
                }
            }

            System.out.println("Palabras prohibidas agregadas o verificadas. Nuevas inserciones: " + inserted);
        } catch (SQLException e) {
            System.err.println("Error al insertar palabras prohibidas: " + e.getMessage());
        }
    }
}
