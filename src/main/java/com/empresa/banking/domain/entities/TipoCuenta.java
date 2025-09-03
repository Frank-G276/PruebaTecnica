package com.empresa.banking.domain.entities;

public enum TipoCuenta {
    CUENTA_AHORROS("Cuenta de Ahorros", "53"),
    CUENTA_CORRIENTE("Cuenta Corriente", "33");

    private final String descripcion;
    private final String prefijo;

    TipoCuenta(String descripcion, String prefijo) {
        this.descripcion = descripcion;
        this.prefijo = prefijo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPrefijo() {
        return prefijo;
    }
}
