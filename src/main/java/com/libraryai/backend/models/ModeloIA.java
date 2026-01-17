package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla ModeloIA
public class ModeloIA {

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
    public ModeloIA(int modelo_id, String nombreModelo, String version, String descripcion,
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

    public int getModelo_id() {
        return modelo_id;
    }

    public void setModelo_id(int modelo_id) {
        this.modelo_id = modelo_id;
    }

    public String getNombreModelo() {
        return nombreModelo;
    }

    public void setNombreModelo(String nombreModelo) {
        this.nombreModelo = nombreModelo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNotasVersion() {
        return notasVersion;
    }

    public void setNotasVersion(String notasVersion) {
        this.notasVersion = notasVersion;
    }

    public LocalDateTime getFechaLanzamiento() {
        return fechaLanzamiento;
    }

    public void setFechaLanzamiento(LocalDateTime fechaLanzamiento) {
        this.fechaLanzamiento = fechaLanzamiento;
    }

    public boolean isEsGratuito() {
        return esGratuito;
    }

    public void setEsGratuito(boolean esGratuito) {
        this.esGratuito = esGratuito;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
