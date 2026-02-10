package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla TokenAcceso
/**
 * Modelo de dominio para token de acceso.
 */
public class AccessToken {

    // **atributos de la tabla privados
    private int token_id;
    private int usuario_id;
    private String tipoToken;
    private String token;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private boolean usado;

    // **Constructor para crear tokens de acceso
    public AccessToken(int token_id, int usuario_id, String tipoToken, String token,
            LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion, boolean usado) {
        this.token_id = token_id;
        this.usuario_id = usuario_id;
        this.tipoToken = tipoToken;
        this.token = token;
        this.fechaCreacion = fechaCreacion;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getTokenId() {
        return token_id;
    }

    public void setTokenId(int token_id) {
        this.token_id = token_id;
    }

    public int getUserId() {
        return usuario_id;
    }

    public void setUserId(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getTokenType() {
        return tipoToken;
    }

    public void setTokenType(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getCreatedAt() {
        return fechaCreacion;
    }

    public void setCreatedAt(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getExpiresAt() {
        return fechaExpiracion;
    }

    public void setExpiresAt(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isUsed() {
        return usado;
    }

    public void setUsed(boolean usado) {
        this.usado = usado;
    }

}
