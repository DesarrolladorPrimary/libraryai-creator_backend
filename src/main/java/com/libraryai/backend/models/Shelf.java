package com.libraryai.backend.models;

/**
 * Representa una estantería/categoría global disponible para organizar relatos.
 */
public class Shelf {
    private int estanteria_id;
    private String nombreCategoria;

    /**
     * Construye la proyección completa de una fila de {@code Estanteria}.
     */
    public Shelf(int estanteria_id, String nombreCategoria) {
        this.estanteria_id = estanteria_id;
        this.nombreCategoria = nombreCategoria;
    }

    public int getShelfId() {
        return estanteria_id;
    }

    public void setShelfId(int estanteria_id) {
        this.estanteria_id = estanteria_id;
    }

    public String getCategoryName() {
        return nombreCategoria;
    }

    public void setCategoryName(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

}
