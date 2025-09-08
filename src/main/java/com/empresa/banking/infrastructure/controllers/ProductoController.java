package com.empresa.banking.infrastructure.controllers;

import com.empresa.banking.domain.entities.EstadoCuenta;
import com.empresa.banking.domain.entities.Producto;
import com.empresa.banking.domain.entities.TipoCuenta;
import com.empresa.banking.domain.entities.TipoTransaccion;
import com.empresa.banking.domain.services.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody CrearProductoRequest request) {
        try {
            Producto producto = productoService.crearProducto(
                    request.getTipoCuenta(),
                    request.getClienteId(),
                    request.getSaldoInicial(),
                    request.getExentaGmf()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(producto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarProductoPorId(@PathVariable Long id) {
        try {
            Optional<Producto> producto = productoService.buscarProductoPorId(id);
            if (producto.isPresent()) {
                return ResponseEntity.ok(producto.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping("/numero-cuenta/{numeroCuenta}")
    public ResponseEntity<?> buscarProductoPorNumeroCuenta(@PathVariable String numeroCuenta) {
        try {
            Optional<Producto> producto = productoService.buscarProductoPorNumeroCuenta(numeroCuenta);
            if (producto.isPresent()) {
                return ResponseEntity.ok(producto.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodosLosProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodosLosProductos();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> obtenerProductosPorCliente(@PathVariable Long clienteId) {
        try {
            List<Producto> productos = productoService.obtenerProductosPorCliente(clienteId);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoProducto(@PathVariable Long id,
                                                   @RequestBody CambiarEstadoRequest request) {
        try {
            Producto producto = productoService.cambiarEstadoProducto(id, request.getNuevoEstado());
            return ResponseEntity.ok(producto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarProducto(@PathVariable Long id) {
        try {
            Producto producto = productoService.activarProducto(id);
            return ResponseEntity.ok(producto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<?> inactivarProducto(@PathVariable Long id) {
        try {
            Producto producto = productoService.inactivarProducto(id);
            return ResponseEntity.ok(producto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarProducto(@PathVariable Long id) {
        try {
            Producto producto = productoService.cancelarProducto(id);
            return ResponseEntity.ok(producto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @PutMapping("/{id}/saldo")
    public ResponseEntity<?> actualizarSaldo(@PathVariable Long id,
                                             @RequestBody ActualizarSaldoRequest request) {
        try {
            Producto producto = productoService.actualizarSaldo(id, request.getNuevoSaldo());
            return ResponseEntity.ok(producto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping("/{id}/puede-transaccion")
    public ResponseEntity<?> puedeRealizarTransaccion(@PathVariable Long id,
                                                      @RequestParam BigDecimal monto,
                                                      @RequestParam TipoTransaccion tipoTransaccion) {
        try {
            boolean puedeRealizar = productoService.puedeRealizarTransaccion(id, monto, tipoTransaccion);
            return ResponseEntity.ok(new TransaccionValidationResponse(puedeRealizar));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    public static class CrearProductoRequest {
        private TipoCuenta tipoCuenta;
        private Long clienteId;
        private BigDecimal saldoInicial;
        private Boolean exentaGmf;

        public TipoCuenta getTipoCuenta() { return tipoCuenta; }
        public void setTipoCuenta(TipoCuenta tipoCuenta) { this.tipoCuenta = tipoCuenta; }
        public Long getClienteId() { return clienteId; }
        public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
        public BigDecimal getSaldoInicial() { return saldoInicial; }
        public void setSaldoInicial(BigDecimal saldoInicial) { this.saldoInicial = saldoInicial; }
        public Boolean getExentaGmf() { return exentaGmf; }
        public void setExentaGmf(Boolean exentaGmf) { this.exentaGmf = exentaGmf; }
    }

    public static class CambiarEstadoRequest {
        private EstadoCuenta nuevoEstado;

        public EstadoCuenta getNuevoEstado() { return nuevoEstado; }
        public void setNuevoEstado(EstadoCuenta nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    }

    public static class ActualizarSaldoRequest {
        private BigDecimal nuevoSaldo;

        public BigDecimal getNuevoSaldo() { return nuevoSaldo; }
        public void setNuevoSaldo(BigDecimal nuevoSaldo) { this.nuevoSaldo = nuevoSaldo; }
    }

    public static class TransaccionValidationResponse {
        private boolean puedeRealizar;

        public TransaccionValidationResponse(boolean puedeRealizar) {
            this.puedeRealizar = puedeRealizar;
        }

        public boolean isPuedeRealizar() { return puedeRealizar; }
        public void setPuedeRealizar(boolean puedeRealizar) { this.puedeRealizar = puedeRealizar; }
    }

    public static class ErrorResponse {
        private String mensaje;

        public ErrorResponse(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}