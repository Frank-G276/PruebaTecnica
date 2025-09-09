package com.empresa.banking.app.services;

import com.empresa.banking.app.interfaces.IProductoService;
import com.empresa.banking.domain.entities.*;
import com.empresa.banking.domain.entities.Enums.EstadoCuenta;
import com.empresa.banking.domain.entities.Enums.TipoCuenta;
import com.empresa.banking.domain.entities.Enums.TipoTransaccion;
import com.empresa.banking.domain.repositories.ClienteRepository;
import com.empresa.banking.domain.repositories.ProductoRepository;
import com.empresa.banking.domain.repositories.TransaccionRepository;
import com.empresa.banking.infrastructure.controllers.ProductoController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService implements IProductoService {

    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final TransaccionRepository transaccionRepository;

    public ProductoService(ProductoRepository productoRepository,
                           ClienteRepository clienteRepository,
                           TransaccionRepository transaccionRepository) {
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.transaccionRepository = transaccionRepository;
    }

    /**
     * Crea un nuevo producto financiero
     */
    public Producto crearProducto(ProductoController.CrearProductoRequest request) {
        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + request.getClienteId()));

        // Crear el producto con número de cuenta auto-generado
        String numeroCuenta;
        do {
            numeroCuenta = generarNumeroCuenta(request.getTipoCuenta());
        } while (productoRepository.existByNumeroCuenta(numeroCuenta));

        Producto nuevoProducto = new Producto(
                null,
                request.getTipoCuenta(),
                numeroCuenta,
                EstadoCuenta.ACTIVA, // Las cuentas se crean activas por defecto
                request.getSaldoInicial() != null ? request.getSaldoInicial() : BigDecimal.ZERO,
                request.getExentaGmf(),
                null,
                null,
                request.getClienteId()
        );

        return productoRepository.save(nuevoProducto);
    }

    /**
     * Busca un producto por ID
     */
    @Transactional(readOnly = true)
    public Optional<Producto> buscarProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * Busca productos por número de cuenta
     */
    @Transactional(readOnly = true)
    public Optional<Producto> buscarProductoPorNumeroCuenta(String numeroCuenta) {
        return productoRepository.findAll().stream()
                .filter(producto -> producto.getNumeroCuenta().equals(numeroCuenta))
                .findFirst();
    }

    /**
     * Obtiene todos los productos
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    /**
     * Obtiene productos por cliente
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorCliente(Long clienteId) {
        return productoRepository.findAll().stream()
                .filter(producto -> producto.getClienteId().equals(clienteId))
                .toList();
    }

    /**
     * Cambia el estado de un producto
     */
    public Producto cambiarEstadoProducto(Long productoId, EstadoCuenta nuevoEstado) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        Producto productoActualizado = producto.cambiarEstado(nuevoEstado);
        return productoRepository.save(productoActualizado);
    }

    /**
     * Activa un producto
     */
    public Producto activarProducto(Long productoId) {
        return cambiarEstadoProducto(productoId, EstadoCuenta.ACTIVA);
    }

    /**
     * Inactiva un producto
     */
    public Producto inactivarProducto(Long productoId) {
        return cambiarEstadoProducto(productoId, EstadoCuenta.INACTIVA);
    }

    /**
     * Cancela un producto (solo si tiene saldo cero)
     */
    public Producto cancelarProducto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        if (!producto.puedeSerCancelada()) {
            throw new IllegalStateException("Solo se pueden cancelar productos con saldo cero");
        }

        // Eliminar todas las transacciones asociadas al producto
        transaccionRepository.deleteByAccountNumber(productoId);

        return cambiarEstadoProducto(productoId, EstadoCuenta.CANCELADA);
    }

    /**
     * Actualiza el saldo de un producto
     */
    public Producto actualizarSaldo(Long productoId, BigDecimal nuevoSaldo) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        Producto productoActualizado = producto.actualizarSaldo(nuevoSaldo);
        return productoRepository.save(productoActualizado);
    }

    /**
     * Verifica si un producto puede realizar una transacción
     */
    @Transactional(readOnly = true)
    public boolean puedeRealizarTransaccion(Long productoId, BigDecimal monto, TipoTransaccion tipoTransaccion) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        return producto.puedeRealizarTransaccion(monto, tipoTransaccion);
    }

    /**
     * Elimina un producto
     */
    public void eliminarProducto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        // Verificar que no tenga saldo
        if (!producto.puedeSerCancelada()) {
            throw new IllegalStateException("No se puede eliminar un producto con saldo diferente a cero");
        }

        // Eliminar transacciones asociadas
        transaccionRepository.deleteByAccountNumber(productoId);

        // Eliminar el producto
        productoRepository.deleteById(productoId);
    }

    /**
     * Genera un número de cuenta único según el tipo
     */
    private String generarNumeroCuenta(TipoCuenta tipoCuenta) {
        String prefijo = tipoCuenta.getPrefijo();
        String sufijo = String.format("%08d", (int)(Math.random() * 100000000));
        return prefijo + sufijo;
    }

    /**
     * Valida que un producto existe y lo devuelve
     */
    @Transactional(readOnly = true)
    public Producto validarExistenciaProducto(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));
    }

    /**
     * Valida que un producto existe por número de cuenta y lo devuelve
     */
    @Transactional(readOnly = true)
    public Producto validarExistenciaProductoPorNumeroCuenta(String numeroCuenta) {
        return buscarProductoPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con número de cuenta: " + numeroCuenta));
    }
}