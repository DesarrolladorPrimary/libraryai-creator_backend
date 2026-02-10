package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla RelatoVersion
/**
 * Modelo de dominio para version de relato.
 */
public class StoryVersion {

    // **atributos de la tabla privados
    private int version_id;
    private int relato_id;
    private float numeroVersion;
    private String contenido;
    private String notas;
    private boolean esPublicada;
    private LocalDateTime fechaVersion;

    // **Constructor para crear versiones de relato
    public StoryVersion(int version_id, int relato_id, float numeroVersion, String contenido,
            String notas, boolean esPublicada, LocalDateTime fechaVersion) {
        this.version_id = version_id;
        this.relato_id = relato_id;
        this.numeroVersion = numeroVersion;
        this.contenido = contenido;
        this.notas = notas;
        this.esPublicada = esPublicada;
        this.fechaVersion = fechaVersion;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getVersionId() {
        return version_id;
    }

    public void setVersionId(int version_id) {
        this.version_id = version_id;
    }

    public int getStoryId() {
        return relato_id;
    }

    public void setStoryId(int relato_id) {
        this.relato_id = relato_id;
    }

    public float getVersionNumber() {
        return numeroVersion;
    }

    public void setVersionNumber(float numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public String getContent() {
        return contenido;
    }

    public void setContent(String contenido) {
        this.contenido = contenido;
    }

    public String getNotes() {
        return notas;
    }

    public void setNotes(String notas) {
        this.notas = notas;
    }

    public boolean isPublished() {
        return esPublicada;
    }

    public void setPublished(boolean esPublicada) {
        this.esPublicada = esPublicada;
    }

    public LocalDateTime getVersionDate() {
        return fechaVersion;
    }

    public void setVersionDate(LocalDateTime fechaVersion) {
        this.fechaVersion = fechaVersion;
    }

}
