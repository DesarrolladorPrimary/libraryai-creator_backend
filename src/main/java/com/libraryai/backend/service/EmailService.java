package com.libraryai.backend.service;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Servicio para enviar correos electrónicos.
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String CORREO_REMITENTE = Dotenv.load().get("EMAIL_USER");
    private static final String CONTRASEÑA_APP = Dotenv.load().get("EMAIL_PASS");

    /**
     * Envía un correo electrónico.
     * 
     * @param correoDestino Correo del destinatario.
     * @param asunto Asunto del correo.
     * @param cuerpo Contenido del correo.
     * @return true si se envió correctamente, false si hubo error.
     */
    public static boolean enviarCorreo(String correoDestino, String asunto, String cuerpo) {
        
        // Configuración del servidor SMTP
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", SMTP_HOST);
        propiedades.put("mail.smtp.port", SMTP_PORT);
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        
        try {
            // Crear sesión con autenticación
            Session session = Session.getInstance(propiedades, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(CORREO_REMITENTE, CONTRASEÑA_APP);
                }
            });

            // Crear el mensaje
            MimeMessage mensaje = new MimeMessage(session);
            
            // Configurar remitente
            mensaje.setFrom(new InternetAddress(CORREO_REMITENTE, "Library Creator"));
            
            // Configurar destinatario
            mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(correoDestino));
            
            // Configurar asunto y cuerpo
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo, "utf-8", "html");
            
            // Enviar mensaje
            Transport.send(mensaje);
            
            System.out.println("Correo enviado exitosamente a: " + correoDestino);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía correo de recuperación de contraseña.
     * 
     * @param correoDestino Correo del usuario.
     * @param token Token de recuperación.
     * @return true si se envió correctamente.
     */
    public static boolean enviarCorreoRecuperacion(String correoDestino, String token) {
        String link = "http://localhost:5500/public/auth/recovery_passwd_change.html?token=" + token;
        
        String asunto = "Recuperación de contraseña - Library Creator";
        String cuerpo = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>Recuperación de contraseña</h2>
                <p>Hola,</p>
                <p>Has solicitado recuperar tu contraseña en Library Creator.</p>
                <p>Haz clic en el siguiente enlace para crear una nueva contraseña:</p>
                <p>
                    <a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Recuperar contraseña
                    </a>
                </p>
                <p>O copia y pega este enlace en tu navegador:</p>
                <p style="color: #666;">%s</p>
                <hr>
                <p style="color: #999; font-size: 12px;">
                    Este enlace expirará en 1 hora. Si no solicitaste este correo, ignóralo.
                </p>
            </body>
            </html>
            """.formatted(link, link);
        
        return enviarCorreo(correoDestino, asunto, cuerpo);
    }
}
