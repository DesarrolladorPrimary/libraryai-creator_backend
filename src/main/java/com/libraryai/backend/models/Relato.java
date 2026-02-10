package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla Relato
/**
 * Modelo de dominio para relato.
 */
public class Relato {

    // **atributos de la tabla privados
    private int relato_id;
    private int usuario_id;
    private Integer estanteria_id;
    private Integer modeloUsado_id;
    private String titulo;
    private String modoOrigen;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    // **Constructor para crear relatos
    public Relato(int relato_id, int usuario_id, Integer estanteria_id, Integer modeloUsado_id,
            String titulo, String modoOrigen, String descripcion,
            LocalDateTime fechaCreacion, LocalDateTime fechaModificacion) {
        this.relato_id = relato_id;
        this.usuario_id = usuario_id;
        this.estanteria_id = estanteria_id;
        this.modeloUsado_id = modeloUsado_id;
        this.titulo = titulo;
        this.modoOrigen = modoOrigen;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getRelato_id() {
        return relato_id;
    }

    public void setRelato_id(int relato_id) {
        this.relato_id = relato_id;
    }

    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public Integer getEstanteria_id() {
        return estanteria_id;
    }

    public void setEstanteria_id(Integer estanteria_id) {
        this.estanteria_id = estanteria_id;
    }

    public Integer getModeloUsado_id() {
        return modeloUsado_id;
    }

    public void setModeloUsado_id(Integer modeloUsado_id) {
        this.modeloUsado_id = modeloUsado_id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getModoOrigen() {
        return modoOrigen;
    }

    public void setModoOrigen(String modoOrigen) {
        this.modoOrigen = modoOrigen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

}
