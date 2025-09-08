package com.empresa.banking.domain.services;

import com.empresa.banking.domain.entities.*;
import com.empresa.banking.domain.repositories.ProductoRepository;
import com.empresa.banking.domain.repositories.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final ProductoRepository productoRepository;

    public TransaccionService(TransaccionRepository transaccionRepository,
                              ProductoRepository productoRepository) {
        this.transaccionRepository = transaccionRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Realiza una consignación
     */
    public Transaccion realizarConsignacion(Long cuentaId, BigDecimal monto, String descripcion) {
        Producto cuenta = validarCuenta(cuentaId);

        if (!cuenta.estaActiva()) {
            throw new IllegalStateException("No se puede realizar transacciones en una cuenta inactiva");
        }

        BigDecimal saldoAnterior = cuenta.getSaldo();
        BigDecimal nuevoSaldo = saldoAnterior.add(monto);

        // Actualizar saldo de la cuenta
        Producto cuentaActualizada = cuenta.actualizarSaldo(nuevoSaldo);
        productoRepository.save(cuentaActualizada);

        // Crear y guardar la transacción
        Transaccion transaccion = Transaccion.crear(
                TipoTransaccion.CONSIGNACION,
                monto,
                cuentaId,
                null, // No hay cuenta destino en consignaciones
                descripcion != null ? descripcion : "Consignación"
        );

        transaccion = transaccion.conSaldos(saldoAnterior, nuevoSaldo);
        return transaccionRepository.save(transaccion);
    }

    /**
     * Realiza un retiro
     */
    public Transaccion realizarRetiro(Long cuentaId, BigDecimal monto, String descripcion) {
        Producto cuenta = validarCuenta(cuentaId);

        if (!cuenta.puedeRealizarTransaccion(monto, TipoTransaccion.RETIRO)) {
            throw new IllegalStateException("No se puede realizar el retiro. Fondos insuficientes o cuenta inactiva");
        }

        BigDecimal saldoAnterior = cuenta.getSaldo();
        BigDecimal nuevoSaldo = saldoAnterior.subtract(monto);

        // Actualizar saldo de la cuenta
        Producto cuentaActualizada = cuenta.actualizarSaldo(nuevoSaldo);
        productoRepository.save(cuentaActualizada);

        // Crear y guardar la transacción
        Transaccion transaccion = Transaccion.crear(
                TipoTransaccion.RETIRO,
                monto,
                cuentaId,
                null, // No hay cuenta destino en retiros
                descripcion != null ? descripcion : "Retiro"
        );

        transaccion = transaccion.conSaldos(saldoAnterior, nuevoSaldo);
        return transaccionRepository.save(transaccion);
    }

    /**
     * Realiza una transferencia entre cuentas
     */
    public List<Transaccion> realizarTransferencia(Long cuentaOrigenId, Long cuentaDestinoId,
                                                   BigDecimal monto, String descripcion) {
        // Validar que las cuentas existan y sean diferentes
        if (cuentaOrigenId.equals(cuentaDestinoId)) {
            throw new IllegalArgumentException("La cuenta origen y destino no pueden ser iguales");
        }

        Producto cuentaOrigen = validarCuenta(cuentaOrigenId);
        Producto cuentaDestino = validarCuenta(cuentaDestinoId);

        // Validar que ambas cuentas estén activas
        if (!cuentaOrigen.estaActiva() || !cuentaDestino.estaActiva()) {
            throw new IllegalStateException("Ambas cuentas deben estar activas para realizar una transferencia");
        }

        // Validar que la cuenta origen puede realizar la transferencia
        if (!cuentaOrigen.puedeRealizarTransaccion(monto, TipoTransaccion.TRANSFERENCIA)) {
            throw new IllegalStateException("Fondos insuficientes en la cuenta origen");
        }

        // Realizar débito en cuenta origen
        BigDecimal saldoAnteriorOrigen = cuentaOrigen.getSaldo();
        BigDecimal nuevoSaldoOrigen = saldoAnteriorOrigen.subtract(monto);

        Producto cuentaOrigenActualizada = cuentaOrigen.actualizarSaldo(nuevoSaldoOrigen);
        productoRepository.save(cuentaOrigenActualizada);

        // Realizar crédito en cuenta destino
        BigDecimal saldoAnteriorDestino = cuentaDestino.getSaldo();
        BigDecimal nuevoSaldoDestino = saldoAnteriorDestino.add(monto);

        Producto cuentaDestinoActualizada = cuentaDestino.actualizarSaldo(nuevoSaldoDestino);
        productoRepository.save(cuentaDestinoActualizada);

        // Crear transacción de débito (cuenta origen)
        Transaccion transaccionDebito = Transaccion.crear(
                TipoTransaccion.TRANSFERENCIA,
                monto,
                cuentaOrigenId,
                cuentaDestinoId,
                descripcion != null ? descripcion : "Transferencia enviada"
        );
        transaccionDebito = transaccionDebito.conSaldos(saldoAnteriorOrigen, nuevoSaldoOrigen);
        transaccionDebito = transaccionRepository.save(transaccionDebito);

        // Crear transacción de crédito (cuenta destino)
        Transaccion transaccionCredito = Transaccion.crear(
                TipoTransaccion.CONSIGNACION, // Se registra como consignación en la cuenta destino
                monto,
                cuentaDestinoId,
                cuentaOrigenId, // Referencia a la cuenta origen
                descripcion != null ? ("Transferencia recibida: " + descripcion) : "Transferencia recibida"
        );
        transaccionCredito = transaccionCredito.conSaldos(saldoAnteriorDestino, nuevoSaldoDestino);
        transaccionCredito = transaccionRepository.save(transaccionCredito);

        return List.of(transaccionDebito, transaccionCredito);
    }

    /**
     * Obtiene el historial de transacciones de una cuenta
     */
    @Transactional(readOnly = true)
    public List<Transaccion> obtenerHistorialTransacciones(Long cuentaId) {
        validarCuenta(cuentaId);
        return transaccionRepository.findByAccountNumber(cuentaId);
    }

    /**
     * Busca una transacción por ID
     */
    @Transactional(readOnly = true)
    public Optional<Transaccion> buscarTransaccionPorId(Long transaccionId) {
        return transaccionRepository.findById(transaccionId);
    }

    /**
     * Obtiene todas las transacciones
     */
    @Transactional(readOnly = true)
    public List<Transaccion> obtenerTodasLasTransacciones() {
        return transaccionRepository.findAll();
    }

    /**
     * Consulta el estado de cuenta (saldo actual) de un producto
     */
    @Transactional(readOnly = true)
    public EstadoCuentaDto consultarEstadoCuenta(Long cuentaId) {
        Producto cuenta = validarCuenta(cuentaId);
        List<Transaccion> transacciones = transaccionRepository.findByAccountNumber(cuentaId);

        return new EstadoCuentaDto(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getEstado(),
                cuenta.getSaldo(),
                cuenta.getFechaCreacion(),
                transacciones
        );
    }

    /**
     * Elimina una transacción (solo para casos administrativos)
     */
    public void eliminarTransaccion(Long transaccionId) {
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con ID: " + transaccionId));

        // Nota: En un sistema bancario real, las transacciones no se eliminan sino que se reversan
        // Esta funcionalidad debe usarse con extrema precaución
        transaccionRepository.deleteById(transaccionId);
    }

    /**
     * Valida que una cuenta existe y la devuelve
     */
    private Producto validarCuenta(Long cuentaId) {
        return productoRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con ID: " + cuentaId));
    }

    /**
     * DTO para consulta de estado de cuenta
     */
    public static class EstadoCuentaDto {
        private final Long cuentaId;
        private final String numeroCuenta;
        private final TipoCuenta tipoCuenta;
        private final EstadoCuenta estado;
        private final BigDecimal saldoActual;
        private final LocalDateTime fechaCreacion;
        private final List<Transaccion> transacciones;

        public EstadoCuentaDto(Long cuentaId, String numeroCuenta, TipoCuenta tipoCuenta,
                               EstadoCuenta estado, BigDecimal saldoActual,
                               LocalDateTime fechaCreacion, List<Transaccion> transacciones) {
            this.cuentaId = cuentaId;
            this.numeroCuenta = numeroCuenta;
            this.tipoCuenta = tipoCuenta;
            this.estado = estado;
            this.saldoActual = saldoActual;
            this.fechaCreacion = fechaCreacion;
            this.transacciones = transacciones;
        }

        // Getters
        public Long getCuentaId() { return cuentaId; }
        public String getNumeroCuenta() { return numeroCuenta; }
        public TipoCuenta getTipoCuenta() { return tipoCuenta; }
        public EstadoCuenta getEstado() { return estado; }
        public BigDecimal getSaldoActual() { return saldoActual; }
        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
        public List<Transaccion> getTransacciones() { return transacciones; }
    }
}