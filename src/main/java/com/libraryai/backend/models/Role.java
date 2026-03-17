package com.libraryai.backend.models;

/**
 * Representa un rol amplio del sistema, usado para autorización por rutas.
 */
public class Role {
    private int rol_id;
    private String nombreRol;

    /**
     * Construye la proyección completa de una fila de {@code Rol}.
     */
    public Role(int rol_id, String nombreRol) {
        this.rol_id = rol_id;
        this.nombreRol = nombreRol;
    }

    public int getRoleId() {
        return rol_id;
    }

    public void setRoleId(int rol_id) {
        this.rol_id = rol_id;
    }

    public String getRoleName() {
        return nombreRol;
    }

    public void setRoleName(String nombreRol) {
        this.nombreRol = nombreRol;
    }

}
