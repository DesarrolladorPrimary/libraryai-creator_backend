package com.libraryai.backend.models;

import java.time.LocalDate;

/**
 * Representa una cuenta de usuario persistida en el sistema.
 */
public class User {
    private int usuarioId;
    private String nombre;
    private String correo;
    private String contrasenaHash;
    private LocalDate fechaRegistro;
    private boolean activo;

    /**
     * Construye la proyección completa de una fila de {@code Usuario}.
     */
    public User(int usuarioId, String nombre, String correo, String contrasenaHash, LocalDate fechaRegistro,
            boolean activo) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public int getUserId() {
        return usuarioId;
    }

    public void setUserId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getName() {
        return nombre;
    }

    public void setName(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return correo;
    }

    public void setEmail(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return contrasenaHash;
    }

    public void setPasswordHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public LocalDate getRegistrationDate() {
        return fechaRegistro;
    }

    public void setRegistrationDate(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActive() {
        return activo;
    }

    public void setActive(boolean activo) {
        this.activo = activo;
    }

}
