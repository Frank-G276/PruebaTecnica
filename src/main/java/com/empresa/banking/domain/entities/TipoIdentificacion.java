package com.empresa.banking.domain.entities;

public enum TipoIdentificacion {
    CEDULA_CIUDADANIA("CC", "Cédula de Ciudadanía"),
    CEDULA_EXTRANJERIA("CE", "Cédula de Extranjería"),
    TARJETA_IDENTIDAD("TI", "Tarjeta de Identidad"),
    PASAPORTE("PA", "Pasaporte"),
    NIT("NIT", "Número de Identificación Tributaria");

    private final String codigo;
    private final String descripcion;

    TipoIdentificacion(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
}