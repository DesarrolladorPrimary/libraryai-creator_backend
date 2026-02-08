package com.libraryai.backend.models;

//*Clase modelo de la tabla Shelf
/**
 * Modelo de dominio para estanteria.
 */
public class Shelf {

    // **atributos de la tabla privados
    private int estanteria_id;
    private int usuario_id;
    private String nombreCategoria;

    // **Constructor para crear estanterias
    public Shelf(int estanteria_id, int usuario_id, String nombreCategoria) {
        this.estanteria_id = estanteria_id;
        this.usuario_id = usuario_id;
        this.nombreCategoria = nombreCategoria;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getEstanteria_id() {
        return estanteria_id;
    }

    public void setEstanteria_id(int estanteria_id) {
        this.estanteria_id = estanteria_id;
    }

    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

}
