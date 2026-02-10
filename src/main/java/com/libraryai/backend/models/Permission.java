package com.libraryai.backend.models;

//*Clase modelo de la tabla Permission
/**
 * Modelo de dominio para permiso.
 */
public class Permission {

    // **atributos de la tabla privados
    private int permiso_id;
    private String clavePermiso;
    private String descripcion;

    // **Constructor para crear permisos
    public Permission(int permiso_id, String clavePermiso, String descripcion) {
        this.permiso_id = permiso_id;
        this.clavePermiso = clavePermiso;
        this.descripcion = descripcion;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getPermiso_id() {
        return permiso_id;
    }

    public void setPermiso_id(int permiso_id) {
        this.permiso_id = permiso_id;
    }

    public String getClavePermiso() {
        return clavePermiso;
    }

    public void setClavePermiso(String clavePermiso) {
        this.clavePermiso = clavePermiso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
