package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla RelatoVersion
public class RelatoVersion {

    // **atributos de la tabla privados
    private int version_id;
    private int relato_id;
    private float numeroVersion;
    private String contenido;
    private String notas;
    private boolean esPublicada;
    private LocalDateTime fechaVersion;

    // **Constructor para crear versiones de relato
    public RelatoVersion(int version_id, int relato_id, float numeroVersion, String contenido,
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

    public int getVersion_id() {
        return version_id;
    }

    public void setVersion_id(int version_id) {
        this.version_id = version_id;
    }

    public int getRelato_id() {
        return relato_id;
    }

    public void setRelato_id(int relato_id) {
        this.relato_id = relato_id;
    }

    public float getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(float numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public boolean isEsPublicada() {
        return esPublicada;
    }

    public void setEsPublicada(boolean esPublicada) {
        this.esPublicada = esPublicada;
    }

    public LocalDateTime getFechaVersion() {
        return fechaVersion;
    }

    public void setFechaVersion(LocalDateTime fechaVersion) {
        this.fechaVersion = fechaVersion;
    }

}
