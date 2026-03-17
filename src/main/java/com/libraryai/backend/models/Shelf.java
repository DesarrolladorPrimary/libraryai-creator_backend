package com.libraryai.backend.models;

/**
 * Representa una estantería/categoría creada por el usuario para organizar su biblioteca.
 */
public class Shelf {
    private int estanteria_id;
    private int usuario_id;
    private String nombreCategoria;

    /**
     * Construye la proyección completa de una fila de {@code Estanteria}.
     */
    public Shelf(int estanteria_id, int usuario_id, String nombreCategoria) {
        this.estanteria_id = estanteria_id;
        this.usuario_id = usuario_id;
        this.nombreCategoria = nombreCategoria;
    }

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
