package com.libraryai.backend.models;

import java.time.LocalDate;

//*Clase modelo de la tabla Usuarios
/**
 * Modelo de dominio para usuario.
 */
public class User {

    // **atributos de la tabla y del usuario privados
    private int usuarioId;
    private String nombre;
    private String correo;
    private String contrasenaHash;
    private LocalDate fechaRegistro;
    private boolean activo;

    // **Constructor para crear usuarios
    public User(int usuarioId, String nombre, String correo, String contrasenaHash, LocalDate fechaRegistro,
            boolean activo) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    // **Getter y Setter para manipulacion de datos privados
    
    public int getUserId() {
        return usuarioId;
    }

    public void setUserId(int usuario_id) {
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
