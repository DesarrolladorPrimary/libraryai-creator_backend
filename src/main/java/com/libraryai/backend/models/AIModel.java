package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla ModeloIA
/**
 * Modelo de dominio para modelo de IA.
 */
public class AIModel {

    // **atributos de la tabla privados
    private int modelo_id;
    private String nombreModelo;
    private String version;
    private String descripcion;
    private String notasVersion;
    private LocalDateTime fechaLanzamiento;
    private boolean esGratuito;
    private String estado;

    // **Constructor para crear modelos de IA
    public AIModel(int modelo_id, String nombreModelo, String version, String descripcion,
            String notasVersion, LocalDateTime fechaLanzamiento, boolean esGratuito, String estado) {
        this.modelo_id = modelo_id;
        this.nombreModelo = nombreModelo;
        this.version = version;
        this.descripcion = descripcion;
        this.notasVersion = notasVersion;
        this.fechaLanzamiento = fechaLanzamiento;
        this.esGratuito = esGratuito;
        this.estado = estado;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getModelId() {
        return modelo_id;
    }

    public void setModelId(int modelo_id) {
        this.modelo_id = modelo_id;
    }

    public String getModelName() {
        return nombreModelo;
    }

    public void setModelName(String nombreModelo) {
        this.nombreModelo = nombreModelo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return descripcion;
    }

    public void setDescription(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getVersionNotes() {
        return notasVersion;
    }

    public void setVersionNotes(String notasVersion) {
        this.notasVersion = notasVersion;
    }

    public LocalDateTime getReleaseDate() {
        return fechaLanzamiento;
    }

    public void setReleaseDate(LocalDateTime fechaLanzamiento) {
        this.fechaLanzamiento = fechaLanzamiento;
    }

    public boolean isFree() {
        return esGratuito;
    }

    public void setFree(boolean esGratuito) {
        this.esGratuito = esGratuito;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
