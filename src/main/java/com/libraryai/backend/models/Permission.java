package com.libraryai.backend.models;

/**
 * Representa un permiso fino dentro del esquema RBAC de la base de datos.
 */
public class Permission {
    private int permiso_id;
    private String clavePermiso;
    private String descripcion;

    /**
     * Construye la proyección completa de una fila de {@code Permiso}.
     */
    public Permission(int permiso_id, String clavePermiso, String descripcion) {
        this.permiso_id = permiso_id;
        this.clavePermiso = clavePermiso;
        this.descripcion = descripcion;
    }

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
