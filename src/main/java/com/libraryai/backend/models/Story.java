package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla Relato
/**
 * Modelo de dominio para relato.
 */
public class Story {

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
    public Story(int relato_id, int usuario_id, Integer estanteria_id, Integer modeloUsado_id,
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

    public int getStoryId() {
        return relato_id;
    }

    public void setStoryId(int relato_id) {
        this.relato_id = relato_id;
    }

    public int getUserId() {
        return usuario_id;
    }

    public void setUserId(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public Integer getShelfId() {
        return estanteria_id;
    }

    public void setShelfId(Integer estanteria_id) {
        this.estanteria_id = estanteria_id;
    }

    public Integer getUsedModelId() {
        return modeloUsado_id;
    }

    public void setUsedModelId(Integer modeloUsado_id) {
        this.modeloUsado_id = modeloUsado_id;
    }

    public String getTitle() {
        return titulo;
    }

    public void setTitle(String titulo) {
        this.titulo = titulo;
    }

    public String getOriginMode() {
        return modoOrigen;
    }

    public void setOriginMode(String modoOrigen) {
        this.modoOrigen = modoOrigen;
    }

    public String getDescription() {
        return descripcion;
    }

    public void setDescription(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getCreatedAt() {
        return fechaCreacion;
    }

    public void setCreatedAt(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getUpdatedAt() {
        return fechaModificacion;
    }

    public void setUpdatedAt(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

}
