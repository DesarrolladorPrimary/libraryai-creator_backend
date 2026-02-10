package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla MensajeChat
/**
 * Modelo de dominio para mensaje de chat.
 */
public class ChatMessage {

    // **atributos de la tabla privados
    private int mensaje_id;
    private int relato_id;
    private String emisor;
    private String contenidoMensaje;
    private LocalDateTime fechaEnvio;
    private int orden;

    // **Constructor para crear mensajes de chat
    public ChatMessage(int mensaje_id, int relato_id, String emisor, String contenidoMensaje,
            LocalDateTime fechaEnvio, int orden) {
        this.mensaje_id = mensaje_id;
        this.relato_id = relato_id;
        this.emisor = emisor;
        this.contenidoMensaje = contenidoMensaje;
        this.fechaEnvio = fechaEnvio;
        this.orden = orden;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getMessageId() {
        return mensaje_id;
    }

    public void setMessageId(int mensaje_id) {
        this.mensaje_id = mensaje_id;
    }

    public int getStoryId() {
        return relato_id;
    }

    public void setStoryId(int relato_id) {
        this.relato_id = relato_id;
    }

    public String getSender() {
        return emisor;
    }

    public void setSender(String emisor) {
        this.emisor = emisor;
    }

    public String getMessageContent() {
        return contenidoMensaje;
    }

    public void setMessageContent(String contenidoMensaje) {
        this.contenidoMensaje = contenidoMensaje;
    }

    public LocalDateTime getSentAt() {
        return fechaEnvio;
    }

    public void setSentAt(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

}
