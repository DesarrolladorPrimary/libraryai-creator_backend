package com.libraryai.backend.models;

import java.math.BigDecimal;

/**
 * Representa un plan de suscripción disponible para los usuarios.
 */
public class SubscriptionPlan {
    private int plan_id;
    private String nombrePlan;
    private int almacenamientoMaxMB;
    private BigDecimal precio;
    private boolean activo;

    /**
     * Construye la proyección completa de una fila de {@code PlanSuscripcion}.
     */
    public SubscriptionPlan(int plan_id, String nombrePlan, int almacenamientoMaxMB,
            BigDecimal precio, boolean activo) {
        this.plan_id = plan_id;
        this.nombrePlan = nombrePlan;
        this.almacenamientoMaxMB = almacenamientoMaxMB;
        this.precio = precio;
        this.activo = activo;
    }

    public int getPlanId() {
        return plan_id;
    }

    public void setPlanId(int plan_id) {
        this.plan_id = plan_id;
    }

    public String getPlanName() {
        return nombrePlan;
    }

    public void setPlanName(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public int getMaxStorageMb() {
        return almacenamientoMaxMB;
    }

    public void setMaxStorageMb(int almacenamientoMaxMB) {
        this.almacenamientoMaxMB = almacenamientoMaxMB;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public boolean isActive() {
        return activo;
    }

    public void setActive(boolean activo) {
        this.activo = activo;
    }

}
