package com.libraryai.backend.models;

import java.time.LocalDate;

//*Clase modelo de la tabla Usuarios
/**
 * Modelo de dominio para usuario.
 */
public class Usuario {

    // **atributos de la tabla y del usuario privados
    private int usuario_id;
    private String nombre;
    private String correo;
    private String contrasenaHash;
    private LocalDate fechaRegistro;
    private boolean activo;

    // **Constructor para crear usuarios
    public Usuario(int usuario_id, String nombre, String correo, String contrasenaHash, LocalDate fechaRegistro,
            boolean activo) {
        this.usuario_id = usuario_id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    // **Getter y Setter para manipulacion de datos privados
    
    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

}
