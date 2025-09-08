package com.empresa.banking.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaccion {

    private final Long id;
    private final TipoTransaccion tipoTransaccion;
    private final BigDecimal monto;
    private final String descripcion;
    private final LocalDateTime fechaTransaccion;
    private final Long cuentaOrigenId;
    private final Long cuentaDestinoId; // Null para consignaciones y retiros
    private final BigDecimal saldoAnterior;
    private final BigDecimal saldoActual;

    // Constructor principal
    public Transaccion(Long id, TipoTransaccion tipoTransaccion, BigDecimal monto,
                       String descripcion, LocalDateTime fechaTransaccion,
                       Long cuentaOrigenId, Long cuentaDestinoId,
                       BigDecimal saldoAnterior, BigDecimal saldoActual) {
        this.id = id;
        this.tipoTransaccion = Objects.requireNonNull(tipoTransaccion, "El tipo de transacción es obligatorio");
        this.monto = validarMonto(monto);
        this.descripcion = descripcion;
        this.fechaTransaccion = fechaTransaccion != null ? fechaTransaccion : LocalDateTime.now();
        this.cuentaOrigenId = Objects.requireNonNull(cuentaOrigenId, "La cuenta de origen es obligatoria");
        this.cuentaDestinoId = cuentaDestinoId;
        this.saldoAnterior = saldoAnterior;
        this.saldoActual = saldoActual;

        validarConsistenciaTransaccion();
    }

    // Constructor para crear nueva transacción
    public static Transaccion crear(TipoTransaccion tipoTransaccion, BigDecimal monto,
                                    Long cuentaOrigenId, Long cuentaDestinoId,
                                    String descripcion) {
        return new Transaccion(null, tipoTransaccion, monto, descripcion,
                LocalDateTime.now(), cuentaOrigenId, cuentaDestinoId,
                null, null);
    }

    // Constructor con saldos actualizados
    public Transaccion conSaldos(BigDecimal saldoAnterior, BigDecimal saldoActual) {
        return new Transaccion(this.id, this.tipoTransaccion, this.monto,
                this.descripcion, this.fechaTransaccion,
                this.cuentaOrigenId, this.cuentaDestinoId,
                saldoAnterior, saldoActual);
    }

    // Validaciones de negocio
    private BigDecimal validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        return monto;
    }

    private void validarConsistenciaTransaccion() {
        if (tipoTransaccion == TipoTransaccion.TRANSFERENCIA && cuentaDestinoId == null) {
            throw new IllegalArgumentException("Una transferencia requiere cuenta destino");
        }

        if (cuentaOrigenId != null && cuentaOrigenId.equals(cuentaDestinoId)) {
            throw new IllegalArgumentException("La cuenta origen y destino no pueden ser iguales");
        }
    }

    // Métodos de negocio
    public boolean esTransferencia() {
        return tipoTransaccion == TipoTransaccion.TRANSFERENCIA;
    }

    public boolean esDebito() {
        return tipoTransaccion == TipoTransaccion.RETIRO ||
                tipoTransaccion == TipoTransaccion.TRANSFERENCIA;
    }

    public boolean esCredito() {
        return tipoTransaccion == TipoTransaccion.CONSIGNACION;
    }

    public BigDecimal getMontoConSigno() {
        return esDebito() ? monto.negate() : monto;
    }

    // Getters
    public Long getId() { return id; }
    public TipoTransaccion getTipoTransaccion() { return tipoTransaccion; }
    public BigDecimal getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFechaTransaccion() { return fechaTransaccion; }
    public Long getCuentaOrigenId() { return cuentaOrigenId; }
    public Long getCuentaDestinoId() { return cuentaDestinoId; }
    public BigDecimal getSaldoAnterior() { return saldoAnterior; }
    public BigDecimal getSaldoActual() { return saldoActual; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaccion that = (Transaccion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
