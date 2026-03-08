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
    private static final Dotenv ENV = Dotenv.load();
    private static final String CORREO_REMITENTE = ENV.get("EMAIL_USER");
    private static final String CONTRASEÑA_APP = ENV.get("EMAIL_PASS");
    private static final String FRONTEND_BASE_URL = resolveFrontendBaseUrl();

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
        String link = buildFrontendUrl("/auth/recovery_passwd_change.html?token=" + token);
        
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

    /**
     * Envía correo de verificación con diseño mejorado y UX optimizada.
     * Se usa cuando el usuario intenta hacer login sin verificar correo.
     * 
     * @param correoDestino Correo del usuario.
     * @param token Token de verificación.
     * @return true si se envió correctamente.
     */
    public static boolean enviarCorreoVerificacionMejorado(String correoDestino, String token) {
        String link = buildFrontendUrl("/auth/verificar.html?token=" + token);
        
        String asunto = "🔔 Verifica tu cuenta - Library Creator";
        String cuerpo = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    <h2 style="color: #2c3e50; text-align: center;">🚀 Bienvenido de nuevo a Library Creator</h2>
                    
                    <p style="font-size: 16px; color: #34495e;">Hola 👋,</p>
                    
                    <p style="font-size: 16px; color: #34495e;">
                        Intentaste iniciar sesión pero necesitamos verificar tu correo electrónico para proteger tu cuenta.
                    </p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #3498db; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; font-size: 16px; font-weight: bold; display: inline-block;">
                            ✅ Verificar mi correo ahora
                        </a>
                    </div>
                    
                    <p style="color: #7f8c8d; text-align: center;">O copia y pega este enlace:</p>
                    <p style="background-color: #ecf0f1; padding: 10px; border-radius: 5px; word-break: break-all; color: #2c3e50; font-family: monospace;">
                        %s
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #ecf0f1; margin: 30px 0;">
                    
                    <div style="color: #95a5a6; font-size: 14px;">
                        <p>📬 <strong>¿No ves el correo?</strong> Revisa tu carpeta de spam o correo no deseado.</p>
                        <p>⏰ <strong>Este enlace expira en 1 hora</strong> por tu seguridad.</p>
                        <p>🔒 <strong>¿No intentaste iniciar sesión?</strong> Ignora este mensaje y tu cuenta seguirá segura.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(link, link);
        
        return enviarCorreo(correoDestino, asunto, cuerpo);
    }

    /**
     * Envía correo de verificación de registro.
     */
    public static boolean enviarCorreoVerificacion(String correoDestino, String token) {
        String link = buildFrontendUrl("/auth/verificar.html?token=" + token);
        
        String asunto = "Verifica tu correo - Library Creator";
        String cuerpo = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>Bienvenido a Library Creator</h2>
                <p>Hola,</p>
                <p>Gracias por registrarte. Para activar tu cuenta, verifica tu correo electrónico.</p>
                <p>Haz clic en el siguiente botón:</p>
                <p>
                    <a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Verificar correo
                    </a>
                </p>
                <p>O copia y pega este enlace en tu navegador:</p>
                <p style="color: #666;">%s</p>
                <hr>
                <p style="color: #999; font-size: 12px;">
                    Este enlace expirará en 24 horas. Si no te registraste, ignóralo.
                </p>
            </body>
            </html>
            """.formatted(link, link);
        
        return enviarCorreo(correoDestino, asunto, cuerpo);
    }

    public static String buildFrontendUrl(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return FRONTEND_BASE_URL + normalizedPath;
    }

    private static String resolveFrontendBaseUrl() {
        String configuredUrl = ENV.get("FRONTEND_BASE_URL");

        if (configuredUrl == null || configuredUrl.isBlank()) {
            return "http://localhost:5500/public";
        }

        return configuredUrl.endsWith("/")
                ? configuredUrl.substring(0, configuredUrl.length() - 1)
                : configuredUrl;
    }
}
