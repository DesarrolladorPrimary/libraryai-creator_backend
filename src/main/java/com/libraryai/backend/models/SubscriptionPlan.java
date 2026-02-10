package com.libraryai.backend.models;

import java.math.BigDecimal;

//*Clase modelo de la tabla PlanSuscripcion
/**
 * Modelo de dominio para plan de suscripcion.
 */
public class SubscriptionPlan {

    // **atributos de la tabla privados
    private int plan_id;
    private String nombrePlan;
    private int almacenamientoMaxMB;
    private BigDecimal precio;
    private boolean activo;

    // **Constructor para crear planes de suscripcion
    public SubscriptionPlan(int plan_id, String nombrePlan, int almacenamientoMaxMB,
            BigDecimal precio, boolean activo) {
        this.plan_id = plan_id;
        this.nombrePlan = nombrePlan;
        this.almacenamientoMaxMB = almacenamientoMaxMB;
        this.precio = precio;
        this.activo = activo;
    }

    // **Getter y Setter para manipulacion de datos privados

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
