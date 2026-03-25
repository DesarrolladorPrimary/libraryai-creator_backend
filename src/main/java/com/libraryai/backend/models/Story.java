package com.libraryai.backend.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el estado actual de un relato dentro del sistema.
 */
public class Story {
    private int relato_id;
    private int usuario_id;
    private List<Integer> estanteria_ids;
    private Integer modeloUsado_id;
    private String titulo;
    private String modoOrigen;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    /**
     * Construye la proyección completa de una fila de {@code Relato}.
     */
    public Story(int relato_id, int usuario_id, List<Integer> estanteria_ids, Integer modeloUsado_id,
            String titulo, String modoOrigen, String descripcion,
            LocalDateTime fechaCreacion, LocalDateTime fechaModificacion) {
        this.relato_id = relato_id;
        this.usuario_id = usuario_id;
        this.estanteria_ids = estanteria_ids != null ? new ArrayList<>(estanteria_ids) : new ArrayList<>();
        this.modeloUsado_id = modeloUsado_id;
        this.titulo = titulo;
        this.modoOrigen = modoOrigen;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }

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

    public List<Integer> getShelfIds() {
        return new ArrayList<>(estanteria_ids);
    }

    public void setShelfIds(List<Integer> estanteria_ids) {
        this.estanteria_ids = estanteria_ids != null ? new ArrayList<>(estanteria_ids) : new ArrayList<>();
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
