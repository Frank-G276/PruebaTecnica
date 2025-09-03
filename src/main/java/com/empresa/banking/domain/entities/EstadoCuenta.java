package com.empresa.banking.domain.entities;

public enum EstadoCuenta {
    ACTIVA("Activa"),
    INACTIVA("Inactiva"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoCuenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
