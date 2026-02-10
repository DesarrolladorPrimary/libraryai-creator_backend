package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla Correo
/**
 * Modelo de dominio para correo.
 */
public class Correo {

    // **atributos de la tabla privados
    private int correo_id;
    private int usuario_id;
    private String asunto;
    private String cuerpo;
    private LocalDateTime fechaEnvio;
    private String estado;

    // **Constructor para crear correos
    public Correo(int correo_id, int usuario_id, String asunto, String cuerpo,
            LocalDateTime fechaEnvio, String estado) {
        this.correo_id = correo_id;
        this.usuario_id = usuario_id;
        this.asunto = asunto;
        this.cuerpo = cuerpo;
        this.fechaEnvio = fechaEnvio;
        this.estado = estado;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getCorreo_id() {
        return correo_id;
    }

    public void setCorreo_id(int correo_id) {
        this.correo_id = correo_id;
    }

    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
