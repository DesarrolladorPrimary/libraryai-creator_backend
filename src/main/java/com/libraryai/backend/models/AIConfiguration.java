package com.libraryai.backend.models;

//*Clase modelo de la tabla ConfiguracionIA
/**
 * Modelo de dominio para configuracion de IA.
 */
public class AIConfiguration {

    // **atributos de la tabla privados
    private int config_id;
    private int relato_id;
    private String estiloEscritura;
    private String nivelCreatividad;
    private String longitudRespuesta;
    private String tonoEmocional;

    // **Constructor para crear configuraciones de IA
    public AIConfiguration(int config_id, int relato_id, String estiloEscritura,
            String nivelCreatividad, String longitudRespuesta, String tonoEmocional) {
        this.config_id = config_id;
        this.relato_id = relato_id;
        this.estiloEscritura = estiloEscritura;
        this.nivelCreatividad = nivelCreatividad;
        this.longitudRespuesta = longitudRespuesta;
        this.tonoEmocional = tonoEmocional;
    }

    // **Getter y Setter para manipulacion de datos privados

    public int getConfigId() {
        return config_id;
    }

    public void setConfigId(int config_id) {
        this.config_id = config_id;
    }

    public int getStoryId() {
        return relato_id;
    }

    public void setStoryId(int relato_id) {
        this.relato_id = relato_id;
    }

    public String getWritingStyle() {
        return estiloEscritura;
    }

    public void setWritingStyle(String estiloEscritura) {
        this.estiloEscritura = estiloEscritura;
    }

    public String getCreativityLevel() {
        return nivelCreatividad;
    }

    public void setCreativityLevel(String nivelCreatividad) {
        this.nivelCreatividad = nivelCreatividad;
    }

    public String getResponseLength() {
        return longitudRespuesta;
    }

    public void setResponseLength(String longitudRespuesta) {
        this.longitudRespuesta = longitudRespuesta;
    }

    public String getEmotionalTone() {
        return tonoEmocional;
    }

    public void setEmotionalTone(String tonoEmocional) {
        this.tonoEmocional = tonoEmocional;
    }

}
