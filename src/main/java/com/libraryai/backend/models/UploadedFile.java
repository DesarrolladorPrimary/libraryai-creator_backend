package com.libraryai.backend.models;

import java.time.LocalDateTime;

//*Clase modelo de la tabla ArchivoSubido
/**
 * Modelo de dominio para archivo subido.
 */
public class UploadedFile {

    // **atributos de la tabla privados
    private int archivo_id;
    private int usuario_id;
    private String nombreArchivo;
    private String tipoArchivo;
    private String rutaAlmacenamiento;
    private int tamanoBytes;
    private LocalDateTime fechaSubida;

    // **Constructor para crear archivos subidos
    public UploadedFile(int archivo_id, int usuario_id, String nombreArchivo, String tipoArchivo,
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

    public int getFileId() {
        return archivo_id;
    }

    public void setFileId(int archivo_id) {
        this.archivo_id = archivo_id;
    }

    public int getUserId() {
        return usuario_id;
    }

    public void setUserId(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getFileName() {
        return nombreArchivo;
    }

    public void setFileName(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getFileType() {
        return tipoArchivo;
    }

    public void setFileType(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public String getStoragePath() {
        return rutaAlmacenamiento;
    }

    public void setStoragePath(String rutaAlmacenamiento) {
        this.rutaAlmacenamiento = rutaAlmacenamiento;
    }

    public int getSizeBytes() {
        return tamanoBytes;
    }

    public void setSizeBytes(int tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public LocalDateTime getUploadedAt() {
        return fechaSubida;
    }

    public void setUploadedAt(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

}
