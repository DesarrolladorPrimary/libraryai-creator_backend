package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla Correo
/**
 * Modelo de dominio para correo.
 */
public class Email {

    // **atributos de la tabla privados
    private int correo_id;
    private int usuario_id;
    private String asunto;
    private String cuerpo;
    private LocalDateTime fechaEnvio;
    private String estado;

    // **Constructor para crear correos
    public Email(int correo_id, int usuario_id, String asunto, String cuerpo,
            LocalDateTime fechaEnvio, String estado) {
        this.correo_id = correo_id;
        this.usuario_id = usuario_id;
        this.asunto = asunto;
        this.cuerpo = cuerpo;
        this.fechaEnvio = fechaEnvio;
        this.estado = estado;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getEmailId() {
        return correo_id;
    }

    public void setEmailId(int correo_id) {
        this.correo_id = correo_id;
    }

    public int getUserId() {
        return usuario_id;
    }

    public void setUserId(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getSubject() {
        return asunto;
    }

    public void setSubject(String asunto) {
        this.asunto = asunto;
    }

    public String getBody() {
        return cuerpo;
    }

    public void setBody(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public LocalDateTime getSentAt() {
        return fechaEnvio;
    }

    public void setSentAt(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
