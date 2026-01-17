package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla Suscripcion
public class Suscripcion {

    // **atributos de la tabla privados
    private int suscripcion_id;
    private int usuario_id;
    private int plan_id;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado;
    private boolean renovacionAutomatica;

    // **Constructor para crear suscripciones
    public Suscripcion(int suscripcion_id, int usuario_id, int plan_id,
            LocalDateTime fechaInicio, LocalDateTime fechaFin,
            String estado, boolean renovacionAutomatica) {
        this.suscripcion_id = suscripcion_id;
        this.usuario_id = usuario_id;
        this.plan_id = plan_id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.renovacionAutomatica = renovacionAutomatica;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getSuscripcion_id() {
        return suscripcion_id;
    }

    public void setSuscripcion_id(int suscripcion_id) {
        this.suscripcion_id = suscripcion_id;
    }

    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public int getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(int plan_id) {
        this.plan_id = plan_id;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isRenovacionAutomatica() {
        return renovacionAutomatica;
    }

    public void setRenovacionAutomatica(boolean renovacionAutomatica) {
        this.renovacionAutomatica = renovacionAutomatica;
    }

}
