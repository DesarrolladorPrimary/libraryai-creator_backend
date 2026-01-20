package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla MensajeChat
/**
 * Modelo de dominio para mensaje de chat.
 */
public class MensajeChat {

    // **atributos de la tabla privados
    private int mensaje_id;
    private int relato_id;
    private String emisor;
    private String contenidoMensaje;
    private LocalDateTime fechaEnvio;
    private int orden;

    // **Constructor para crear mensajes de chat
    public MensajeChat(int mensaje_id, int relato_id, String emisor, String contenidoMensaje,
            LocalDateTime fechaEnvio, int orden) {
        this.mensaje_id = mensaje_id;
        this.relato_id = relato_id;
        this.emisor = emisor;
        this.contenidoMensaje = contenidoMensaje;
        this.fechaEnvio = fechaEnvio;
        this.orden = orden;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getMensaje_id() {
        return mensaje_id;
    }

    public void setMensaje_id(int mensaje_id) {
        this.mensaje_id = mensaje_id;
    }

    public int getRelato_id() {
        return relato_id;
    }

    public void setRelato_id(int relato_id) {
        this.relato_id = relato_id;
    }

    public String getEmisor() {
        return emisor;
    }

    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }

    public String getContenidoMensaje() {
        return contenidoMensaje;
    }

    public void setContenidoMensaje(String contenidoMensaje) {
        this.contenidoMensaje = contenidoMensaje;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

}
