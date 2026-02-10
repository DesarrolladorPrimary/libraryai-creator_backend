package com.libraryai.backend.models;

//*Clase modelo de la tabla Estanteria
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

    public int getShelfId() {
        return estanteria_id;
    }

    public void setShelfId(int estanteria_id) {
        this.estanteria_id = estanteria_id;
    }

    public int getUserId() {
        return usuario_id;
    }

    public void setUserId(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getCategoryName() {
        return nombreCategoria;
    }

    public void setCategoryName(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

}
