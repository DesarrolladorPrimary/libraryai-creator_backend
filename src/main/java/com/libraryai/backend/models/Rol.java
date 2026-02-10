package com.libraryai.backend.models;

//*Clase modelo de la tabla Rol
/**
 * Modelo de dominio para rol.
 */
public class Rol {

    // **atributos de la tabla privados
    private int rol_id;
    private String nombreRol;

    // **Constructor para crear roles
    public Rol(int rol_id, String nombreRol) {
        this.rol_id = rol_id;
        this.nombreRol = nombreRol;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getRol_id() {
        return rol_id;
    }

    public void setRol_id(int rol_id) {
        this.rol_id = rol_id;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

}
