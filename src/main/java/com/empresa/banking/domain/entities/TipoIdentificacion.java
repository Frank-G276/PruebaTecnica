package com.empresa.banking.domain.entities;

public enum TipoIdentificacion {
    CEDULA_CIUDADANIA("Cédula de Ciudadanía"),
    CEDULA_EXTRANJERIA("Cédula de Extranjería"),
    PASAPORTE("Pasaporte"),
    TARJETA_IDENTIDAD("Tarjeta de Identidad");

    private final String descripcion;

    TipoIdentificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
