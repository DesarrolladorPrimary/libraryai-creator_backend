package com.libraryai.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//*Clase modelo de la tabla Pago
public class Pago {

    // **atributos de la tabla privados
    private int pago_id;
    private int suscripcion_id;
    private String pasarela;
    private String estadoPago;
    private String referenciaExterna;
    private BigDecimal monto;
    private LocalDateTime fechaPago;

    // **Constructor para crear pagos
    public Pago(int pago_id, int suscripcion_id, String pasarela, String estadoPago,
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

    public int getPago_id() {
        return pago_id;
    }

    public void setPago_id(int pago_id) {
        this.pago_id = pago_id;
    }

    public int getSuscripcion_id() {
        return suscripcion_id;
    }

    public void setSuscripcion_id(int suscripcion_id) {
        this.suscripcion_id = suscripcion_id;
    }

    public String getPasarela() {
        return pasarela;
    }

    public void setPasarela(String pasarela) {
        this.pasarela = pasarela;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

}
