package com.libraryai.backend.models;

//*Clase modelo de la tabla Rol
/**
 * Modelo de dominio para rol.
 */
public class Role {

    // **atributos de la tabla privados
    private int rol_id;
    private String nombreRol;

    // **Constructor para crear roles
    public Role(int rol_id, String nombreRol) {
        this.rol_id = rol_id;
        this.nombreRol = nombreRol;
    }

    // **Getter y Setter para manipulacion de datos privados

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
