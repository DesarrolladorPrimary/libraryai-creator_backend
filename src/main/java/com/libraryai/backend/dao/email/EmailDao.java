package com.libraryai.backend.dao.email;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.libraryai.backend.config.DatabaseConnection;

/**
 * DAO para registrar y actualizar el historial de correos transaccionales.
 */
public class EmailDao {

    // language=sql
    private static final String SQL_INSERT_EMAIL_LOG = """
            INSERT INTO Correo(FK_UsuarioID, TipoCorreo, Destinatario, Asunto, Cuerpo, Estado)
            VALUES(?, ?, ?, ?, ?, ?)
            """;

    // language=sql
    private static final String SQL_UPDATE_EMAIL_STATUS = """
            UPDATE Correo
            SET Estado = ?, ErrorDetalle = ?
            WHERE PK_CorreoID = ?
            """;

    /**
     * Crea un registro inicial del correo antes del intento de envio.
     *
     * <p>El historial nace como {@code Pendiente} para que luego el servicio de
     * correo pueda marcarlo como enviado o fallido sin perder trazabilidad.
     *
     * @return id generado o 0 si no pudo registrarse.
     */
    public static int createEmailLog(int userId, String tipoCorreo, String destinatario, String asunto, String cuerpo) {
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_EMAIL_LOG, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, tipoCorreo);
            pstmt.setString(3, destinatario);
            pstmt.setString(4, asunto);
            pstmt.setString(5, cuerpo);
            pstmt.setString(6, "Pendiente");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows <= 0) {
                return 0;
            }

            try (var generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar correo en historial: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Actualiza el estado final del correo y el detalle de error si aplica.
     */
    public static void updateEmailStatus(int emailId, String estado, String errorDetalle) {
        if (emailId <= 0) {
            return;
        }

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_EMAIL_STATUS)) {

            pstmt.setString(1, estado);
            if (errorDetalle == null || errorDetalle.isBlank()) {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(2, truncate(errorDetalle, 500));
            }
            pstmt.setInt(3, emailId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del correo: " + e.getMessage());
        }
    }

    /**
     * Ajusta mensajes largos de error al límite persistido por la tabla Correo.
     */
    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
