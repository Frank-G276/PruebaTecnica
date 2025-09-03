package com.empresa.banking.domain.entities;

public enum TipoTransaccion {
    CONSIGNACION("Consignación"),
    RETIRO("Retiro"),
    TRANSFERENCIA("Transferencia");

    private final String descripcion;

    TipoTransaccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
