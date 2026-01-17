package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla ArchivoSubido
public class ArchivoSubido {

    // **atributos de la tabla privados
    private int archivo_id;
    private int usuario_id;
    private String nombreArchivo;
    private String tipoArchivo;
    private String rutaAlmacenamiento;
    private int tamanoBytes;
    private LocalDateTime fechaSubida;

    // **Constructor para crear archivos subidos
    public ArchivoSubido(int archivo_id, int usuario_id, String nombreArchivo, String tipoArchivo,
            String rutaAlmacenamiento, int tamanoBytes, LocalDateTime fechaSubida) {
        this.archivo_id = archivo_id;
        this.usuario_id = usuario_id;
        this.nombreArchivo = nombreArchivo;
        this.tipoArchivo = tipoArchivo;
        this.rutaAlmacenamiento = rutaAlmacenamiento;
        this.tamanoBytes = tamanoBytes;
        this.fechaSubida = fechaSubida;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getArchivo_id() {
        return archivo_id;
    }

    public void setArchivo_id(int archivo_id) {
        this.archivo_id = archivo_id;
    }

    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public String getRutaAlmacenamiento() {
        return rutaAlmacenamiento;
    }

    public void setRutaAlmacenamiento(String rutaAlmacenamiento) {
        this.rutaAlmacenamiento = rutaAlmacenamiento;
    }

    public int getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(int tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

}
