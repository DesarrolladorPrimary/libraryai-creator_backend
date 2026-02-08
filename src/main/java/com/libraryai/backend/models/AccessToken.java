package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla AccessToken
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

    public int getToken_id() {
        return token_id;
    }

    public void setToken_id(int token_id) {
        this.token_id = token_id;
    }

    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }

}
