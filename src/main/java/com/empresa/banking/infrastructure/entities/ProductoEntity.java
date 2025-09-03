package com.empresa.banking.infrastructure.entities;

import com.empresa.banking.domain.entities.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "productos")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false)
    private TipoCuenta tipoCuenta;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 10)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCuenta estado;

    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @Column(name = "exenta_gmf", nullable = false)
    private Boolean exentaGmf;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @OneToMany(mappedBy = "cuentaOrigen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransaccionEntity> transaccionesOrigen = new ArrayList<>();

    @OneToMany(mappedBy = "cuentaDestino", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransaccionEntity> transaccionesDestino = new ArrayList<>();

    // Constructores
    public ProductoEntity() {
    }

    public ProductoEntity(TipoCuenta tipoCuenta, ClienteEntity cliente, Boolean exentaGmf) {
        this.tipoCuenta = tipoCuenta;
        this.cliente = cliente;
        this.saldo = BigDecimal.ZERO;
        this.estado = EstadoCuenta.ACTIVA;
        this.exentaGmf = exentaGmf != null ? exentaGmf : false;
    }

    // Métodos de ciclo de vida JPA
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.numeroCuenta == null) {
            this.numeroCuenta = generarNumeroCuenta();
        }
        if (this.estado == null) {
            this.estado = EstadoCuenta.ACTIVA;
        }
        if (this.saldo == null) {
            this.saldo = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    // Método para generar número de cuenta
    private String generarNumeroCuenta() {
        String prefijo = tipoCuenta.getPrefijo();
        String sufijo = String.format("%08d", new Random().nextInt(100000000));
        return prefijo + sufijo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoCuenta getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(TipoCuenta tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public EstadoCuenta getEstado() { return estado; }
    public void setEstado(EstadoCuenta estado) { this.estado = estado; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public Boolean getExentaGmf() { return exentaGmf; }
    public void setExentaGmf(Boolean exentaGmf) { this.exentaGmf = exentaGmf; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public ClienteEntity getCliente() { return cliente; }
    public void setCliente(ClienteEntity cliente) { this.cliente = cliente; }

    public List<TransaccionEntity> getTransaccionesOrigen() { return transaccionesOrigen; }
    public void setTransaccionesOrigen(List<TransaccionEntity> transaccionesOrigen) { this.transaccionesOrigen = transaccionesOrigen; }

    public List<TransaccionEntity> getTransaccionesDestino() { return transaccionesDestino; }
    public void setTransaccionesDestino(List<TransaccionEntity> transaccionesDestino) { this.transaccionesDestino = transaccionesDestino; }
}
