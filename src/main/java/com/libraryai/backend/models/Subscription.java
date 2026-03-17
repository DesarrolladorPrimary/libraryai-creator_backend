package com.libraryai.backend.models;

import java.time.LocalDateTime;

/**
 * Representa la suscripción vigente o histórica de un usuario a un plan.
 */
public class Subscription {
    private int suscripcion_id;
    private int usuario_id;
    private int plan_id;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado;
    private boolean renovacionAutomatica;

    /**
     * Construye la proyección completa de una fila de {@code Suscripcion}.
     */
    public Subscription(int suscripcion_id, int usuario_id, int plan_id,
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

    public int getSubscriptionId() {
        return suscripcion_id;
    }

    public void setSubscriptionId(int suscripcion_id) {
        this.suscripcion_id = suscripcion_id;
    }

    public int getUserId() {
        return usuario_id;
    }

    public void setUserId(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public int getPlanId() {
        return plan_id;
    }

    public void setPlanId(int plan_id) {
        this.plan_id = plan_id;
    }

    public LocalDateTime getStartDate() {
        return fechaInicio;
    }

    public void setStartDate(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getEndDate() {
        return fechaFin;
    }

    public void setEndDate(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isAutoRenewal() {
        return renovacionAutomatica;
    }

    public void setAutoRenewal(boolean renovacionAutomatica) {
        this.renovacionAutomatica = renovacionAutomatica;
    }

}
