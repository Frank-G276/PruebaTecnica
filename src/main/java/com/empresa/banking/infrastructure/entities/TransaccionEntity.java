package com.empresa.banking.infrastructure.entities;

import com.empresa.banking.domain.entities.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transaccion", nullable = false)
    private TipoTransaccion tipoTransaccion;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDateTime fechaTransaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen_id", nullable = false)
    private ProductoEntity cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id")
    private ProductoEntity cuentaDestino;

    @Column(name = "saldo_anterior", precision = 15, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_actual", precision = 15, scale = 2)
    private BigDecimal saldoActual;

    // Constructores
    public TransaccionEntity() {}

    public TransaccionEntity(TipoTransaccion tipoTransaccion, BigDecimal monto,
                             ProductoEntity cuentaOrigen, ProductoEntity cuentaDestino,
                             String descripcion) {
        this.tipoTransaccion = tipoTransaccion;
        this.monto = monto;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.descripcion = descripcion;
    }

    // Métodos de conversión
    public static TransaccionEntity fromDomain(Transaccion transaccion,
                                               ProductoEntity cuentaOrigen,
                                               ProductoEntity cuentaDestino) {
        TransaccionEntity entity = new TransaccionEntity();
        entity.setId(transaccion.getId());
        entity.setTipoTransaccion(transaccion.getTipoTransaccion());
        entity.setMonto(transaccion.getMonto());
        entity.setDescripcion(transaccion.getDescripcion());
        entity.setFechaTransaccion(transaccion.getFechaTransaccion());
        entity.setCuentaOrigen(cuentaOrigen);
        entity.setCuentaDestino(cuentaDestino);
        entity.setSaldoAnterior(transaccion.getSaldoAnterior());
        entity.setSaldoActual(transaccion.getSaldoActual());
        return entity;
    }

    public Transaccion toDomain() {
        return new Transaccion(id, tipoTransaccion, monto, descripcion,
                fechaTransaccion,
                cuentaOrigen != null ? cuentaOrigen.getId() : null,
                cuentaDestino != null ? cuentaDestino.getId() : null,
                saldoAnterior, saldoActual);
    }

    // Métodos de ciclo de vida JPA
    @PrePersist
    protected void onCreate() {
        if (this.fechaTransaccion == null) {
            this.fechaTransaccion = LocalDateTime.now();
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoTransaccion getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(TipoTransaccion tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaTransaccion() { return fechaTransaccion; }
    public void setFechaTransaccion(LocalDateTime fechaTransaccion) { this.fechaTransaccion = fechaTransaccion; }

    public ProductoEntity getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(ProductoEntity cuentaOrigen) { this.cuentaOrigen = cuentaOrigen; }

    public ProductoEntity getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(ProductoEntity cuentaDestino) { this.cuentaDestino = cuentaDestino; }

    public BigDecimal getSaldoAnterior() { return saldoAnterior; }
    public void setSaldoAnterior(BigDecimal saldoAnterior) { this.saldoAnterior = saldoAnterior; }

    public BigDecimal getSaldoActual() { return saldoActual; }
    public void setSaldoActual(BigDecimal saldoActual) { this.saldoActual = saldoActual; }
}
