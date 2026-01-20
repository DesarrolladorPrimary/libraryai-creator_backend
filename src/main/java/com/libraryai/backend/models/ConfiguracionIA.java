package com.libraryai.backend.models;

//*Clase modelo de la tabla ConfiguracionIA
/**
 * Modelo de dominio para configuracion de IA.
 */
public class ConfiguracionIA {

    // **atributos de la tabla privados
    private int config_id;
    private int relato_id;
    private String estiloEscritura;
    private String nivelCreatividad;
    private String longitudRespuesta;
    private String tonoEmocional;

    // **Constructor para crear configuraciones de IA
    public ConfiguracionIA(int config_id, int relato_id, String estiloEscritura,
            String nivelCreatividad, String longitudRespuesta, String tonoEmocional) {
        this.config_id = config_id;
        this.relato_id = relato_id;
        this.estiloEscritura = estiloEscritura;
        this.nivelCreatividad = nivelCreatividad;
        this.longitudRespuesta = longitudRespuesta;
        this.tonoEmocional = tonoEmocional;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getConfig_id() {
        return config_id;
    }

    public void setConfig_id(int config_id) {
        this.config_id = config_id;
    }

    public int getRelato_id() {
        return relato_id;
    }

    public void setRelato_id(int relato_id) {
        this.relato_id = relato_id;
    }

    public String getEstiloEscritura() {
        return estiloEscritura;
    }

    public void setEstiloEscritura(String estiloEscritura) {
        this.estiloEscritura = estiloEscritura;
    }

    public String getNivelCreatividad() {
        return nivelCreatividad;
    }

    public void setNivelCreatividad(String nivelCreatividad) {
        this.nivelCreatividad = nivelCreatividad;
    }

    public String getLongitudRespuesta() {
        return longitudRespuesta;
    }

    public void setLongitudRespuesta(String longitudRespuesta) {
        this.longitudRespuesta = longitudRespuesta;
    }

    public String getTonoEmocional() {
        return tonoEmocional;
    }

    public void setTonoEmocional(String tonoEmocional) {
        this.tonoEmocional = tonoEmocional;
    }

}
