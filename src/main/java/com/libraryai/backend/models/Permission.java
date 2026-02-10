package com.libraryai.backend.models;

//*Clase modelo de la tabla Permiso
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

    public int getPermissionId() {
        return permiso_id;
    }

    public void setPermissionId(int permiso_id) {
        this.permiso_id = permiso_id;
    }

    public String getPermissionKey() {
        return clavePermiso;
    }

    public void setPermissionKey(String clavePermiso) {
        this.clavePermiso = clavePermiso;
    }

    public String getDescription() {
        return descripcion;
    }

    public void setDescription(String descripcion) {
        this.descripcion = descripcion;
    }

}
