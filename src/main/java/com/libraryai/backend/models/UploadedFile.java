package com.libraryai.backend.models;

import java.time.LocalDateTime;

/**
 * Representa un archivo persistido del usuario, ya sea subido o exportado.
 */
public class UploadedFile {
    private int archivo_id;
    private int usuario_id;
    private String nombreArchivo;
    private String tipoArchivo;
    private String rutaAlmacenamiento;
    private int tamanoBytes;
    private LocalDateTime fechaSubida;

    /**
     * Construye la proyección completa de una fila de {@code ArchivoUsuario}.
     */
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
