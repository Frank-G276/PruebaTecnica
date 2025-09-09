package com.empresa.banking.app.interfaces;

import com.empresa.banking.domain.entities.Producto;
import com.empresa.banking.domain.entities.Enums.EstadoCuenta;
import com.empresa.banking.domain.entities.Enums.TipoCuenta;
import com.empresa.banking.domain.entities.Enums.TipoTransaccion;
import com.empresa.banking.infrastructure.controllers.ProductoController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IProductoService {

    Producto crearProducto(ProductoController.CrearProductoRequest request);

    Optional<Producto> buscarProductoPorId(Long id);

    Optional<Producto> buscarProductoPorNumeroCuenta(String numeroCuenta);

    List<Producto> obtenerTodosLosProductos();

    List<Producto> obtenerProductosPorCliente(Long clienteId);

    Producto cambiarEstadoProducto(Long productoId, EstadoCuenta nuevoEstado);

    Producto activarProducto(Long productoId);

    Producto inactivarProducto(Long productoId);

    Producto cancelarProducto(Long productoId);

    Producto actualizarSaldo(Long productoId, BigDecimal nuevoSaldo);

    boolean puedeRealizarTransaccion(Long productoId, BigDecimal monto, TipoTransaccion tipoTransaccion);

    void eliminarProducto(Long productoId);

    Producto validarExistenciaProducto(Long productoId);

    Producto validarExistenciaProductoPorNumeroCuenta(String numeroCuenta);
}
