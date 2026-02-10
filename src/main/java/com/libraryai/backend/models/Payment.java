package com.libraryai.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//*Clase modelo de la tabla Pago
/**
 * Modelo de dominio para pago.
 */
public class Payment {

    // **atributos de la tabla privados
    private int pago_id;
    private int suscripcion_id;
    private String pasarela;
    private String estadoPago;
    private String referenciaExterna;
    private BigDecimal monto;
    private LocalDateTime fechaPago;

    // **Constructor para crear pagos
    public Payment(int pago_id, int suscripcion_id, String pasarela, String estadoPago,
            String referenciaExterna, BigDecimal monto, LocalDateTime fechaPago) {
        this.pago_id = pago_id;
        this.suscripcion_id = suscripcion_id;
        this.pasarela = pasarela;
        this.estadoPago = estadoPago;
        this.referenciaExterna = referenciaExterna;
        this.monto = monto;
        this.fechaPago = fechaPago;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getPaymentId() {
        return pago_id;
    }

    public void setPaymentId(int pago_id) {
        this.pago_id = pago_id;
    }

    public int getSubscriptionId() {
        return suscripcion_id;
    }

    public void setSubscriptionId(int suscripcion_id) {
        this.suscripcion_id = suscripcion_id;
    }

    public String getGateway() {
        return pasarela;
    }

    public void setGateway(String pasarela) {
        this.pasarela = pasarela;
    }

    public String getPaymentStatus() {
        return estadoPago;
    }

    public void setPaymentStatus(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getExternalReference() {
        return referenciaExterna;
    }

    public void setExternalReference(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public BigDecimal getAmount() {
        return monto;
    }

    public void setAmount(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getPaidAt() {
        return fechaPago;
    }

    public void setPaidAt(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

}
