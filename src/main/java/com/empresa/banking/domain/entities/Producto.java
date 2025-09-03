package com.empresa.banking.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

public class Producto {

    private final Long id;
    private final TipoCuenta tipoCuenta;
    private final String numeroCuenta;
    private final EstadoCuenta estado;
    private final BigDecimal saldo;
    private final Boolean exentaGmf;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaModificacion;
    private final Long clienteId;

    // Constructor principal
    public Producto(Long id, TipoCuenta tipoCuenta, String numeroCuenta,
                    EstadoCuenta estado, BigDecimal saldo, Boolean exentaGmf,
                    LocalDateTime fechaCreacion, LocalDateTime fechaModificacion,
                    Long clienteId) {
        this.id = id;
        this.tipoCuenta = Objects.requireNonNull(tipoCuenta, "El tipo de cuenta es obligatorio");
        this.numeroCuenta = numeroCuenta != null ? numeroCuenta : generarNumeroCuenta(tipoCuenta);
        this.estado = estado != null ? estado : EstadoCuenta.ACTIVA;
        this.saldo = validarSaldo(saldo, tipoCuenta);
        this.exentaGmf = exentaGmf != null ? exentaGmf : false;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.fechaModificacion = fechaModificacion;
        this.clienteId = Objects.requireNonNull(clienteId, "El cliente es obligatorio");
    }

    // Constructor para cambio de estado
    public Producto cambiarEstado(EstadoCuenta nuevoEstado) {
        if (nuevoEstado == EstadoCuenta.CANCELADA && !puedeSerCancelada()) {
            throw new IllegalStateException("Solo se pueden cancelar cuentas con saldo cero");
        }

        return new Producto(this.id, this.tipoCuenta, this.numeroCuenta,
                nuevoEstado, this.saldo, this.exentaGmf,
                this.fechaCreacion, LocalDateTime.now(), this.clienteId);
    }

    // Constructor para actualizar saldo
    public Producto actualizarSaldo(BigDecimal nuevoSaldo) {
        BigDecimal saldoValidado = validarSaldo(nuevoSaldo, this.tipoCuenta);

        return new Producto(this.id, this.tipoCuenta, this.numeroCuenta,
                this.estado, saldoValidado, this.exentaGmf,
                this.fechaCreacion, LocalDateTime.now(), this.clienteId);
    }

    // Validaciones de negocio
    private BigDecimal validarSaldo(BigDecimal saldo, TipoCuenta tipoCuenta) {
        if (saldo == null) {
            return BigDecimal.ZERO;
        }

        if (tipoCuenta == TipoCuenta.CUENTA_AHORROS && saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Una cuenta de ahorros no puede tener saldo negativo");
        }

        return saldo;
    }

    private String generarNumeroCuenta(TipoCuenta tipoCuenta) {
        String prefijo = tipoCuenta.getPrefijo();
        String sufijo = String.format("%08d", new Random().nextInt(100000000));
        return prefijo + sufijo;
    }

    // Métodos de negocio
    public boolean puedeSerCancelada() {
        return saldo.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean estaActiva() {
        return estado == EstadoCuenta.ACTIVA;
    }

    public boolean puedeRealizarTransaccion(BigDecimal monto, TipoTransaccion tipoTransaccion) {
        if (!estaActiva()) {
            return false;
        }

        if (tipoTransaccion == TipoTransaccion.RETIRO || tipoTransaccion == TipoTransaccion.TRANSFERENCIA) {
            if (tipoCuenta == TipoCuenta.CUENTA_AHORROS) {
                return saldo.compareTo(monto) >= 0;
            }
            // Las cuentas corrientes pueden tener sobregiro
        }

        return true;
    }

    // Getters
    public Long getId() { return id; }
    public TipoCuenta getTipoCuenta() { return tipoCuenta; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public EstadoCuenta getEstado() { return estado; }
    public BigDecimal getSaldo() { return saldo; }
    public Boolean getExentaGmf() { return exentaGmf; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public Long getClienteId() { return clienteId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(numeroCuenta, producto.numeroCuenta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroCuenta);
    }
}
