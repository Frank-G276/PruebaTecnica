package com.empresa.banking.infrastructure.controllers;

import com.empresa.banking.domain.entities.EstadoCuenta;
import com.empresa.banking.domain.entities.Producto;
import com.empresa.banking.domain.entities.TipoCuenta;
import com.empresa.banking.domain.entities.TipoTransaccion;
import com.empresa.banking.domain.services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
@Tag(name = "Productos", description = "API para gestión de productos financieros (cuentas bancarias)")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(
            summary = "Crear un nuevo producto financiero",
            description = "Crea una nueva cuenta bancaria (corriente o de ahorros) para un cliente existente. " +
                    "Las cuentas de ahorro inician con '53' y las corrientes con '33'. " +
                    "Se establece como activa por defecto."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o cliente no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<?> crearProducto(@Valid @RequestBody CrearProductoRequest request) {
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

    @Operation(
            summary = "Buscar producto por ID",
            description = "Obtiene la información detallada de un producto financiero por su identificador único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarProductoPorId(
            @Parameter(description = "ID único del producto", required = true)
            @PathVariable Long id) {
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

    @Operation(
            summary = "Buscar producto por número de cuenta",
            description = "Obtiene la información de un producto usando su número de cuenta único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/numero-cuenta/{numeroCuenta}")
    public ResponseEntity<?> buscarProductoPorNumeroCuenta(
            @Parameter(description = "Número de cuenta único (10 dígitos)", example = "5312345678", required = true)
            @PathVariable String numeroCuenta) {
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

    @Operation(
            summary = "Obtener todos los productos",
            description = "Retorna la lista completa de productos financieros registrados en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de productos obtenida exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
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

    @Operation(
            summary = "Obtener productos por cliente",
            description = "Retorna todos los productos financieros asociados a un cliente específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de productos del cliente obtenida exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> obtenerProductosPorCliente(
            @Parameter(description = "ID del cliente propietario de los productos", required = true)
            @PathVariable Long clienteId) {
        try {
            List<Producto> productos = productoService.obtenerProductosPorCliente(clienteId);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @Operation(
            summary = "Cambiar estado del producto",
            description = "Modifica el estado de un producto financiero (ACTIVA, INACTIVA, CANCELADA)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado del producto cambiado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Producto no encontrado o estado inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoProducto(
            @Parameter(description = "ID único del producto", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {
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

    @Operation(
            summary = "Activar producto",
            description = "Cambia el estado de un producto a ACTIVA, permitiendo transacciones"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto activado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarProducto(
            @Parameter(description = "ID único del producto", required = true)
            @PathVariable Long id) {
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

    @Operation(
            summary = "Inactivar producto",
            description = "Cambia el estado de un producto a INACTIVA, bloqueando transacciones temporalmente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto inactivado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}/inactivar")
    public ResponseEntity<?> inactivarProducto(
            @Parameter(description = "ID único del producto", required = true)
            @PathVariable Long id) {
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

    @Operation(
            summary = "Cancelar producto",
            description = "Cancela permanentemente un producto. Solo se permite si el saldo es $0"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto cancelado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede cancelar producto con saldo diferente a cero o producto no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarProducto(
            @Parameter(description = "ID único del producto", required = true)
            @PathVariable Long id) {
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

    @Operation(
            summary = "Actualizar saldo del producto",
            description = "Modifica el saldo actual de un producto financiero. Las cuentas de ahorro no pueden tener saldo negativo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Saldo actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Producto no encontrado o saldo inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}/saldo")
    public ResponseEntity<?> actualizarSaldo(
            @Parameter(description = "ID único del producto", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActualizarSaldoRequest request) {
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

    @Operation(
            summary = "Validar posibilidad de transacción",
            description = "Verifica si es posible realizar una transacción específica en el producto, " +
                    "considerando el saldo disponible y el estado de la cuenta"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Validación realizada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransaccionValidationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}/puede-transaccion")
    public ResponseEntity<?> puedeRealizarTransaccion(
            @Parameter(description = "ID único del producto", required = true)
            @PathVariable Long id,
            @Parameter(description = "Monto de la transacción", example = "100.00", required = true)
            @RequestParam BigDecimal monto,
            @Parameter(description = "Tipo de transacción a realizar", example = "RETIRO", required = true)
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

    @Operation(
            summary = "Eliminar producto",
            description = "Elimina un producto del sistema. Solo se puede eliminar si el saldo es cero."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Producto eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede eliminar producto con saldo o producto no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(
            @Parameter(description = "ID único del producto", required = true)
            @PathVariable Long id) {
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

    @Schema(description = "Datos requeridos para crear un nuevo producto financiero")
    public static class CrearProductoRequest {

        @Schema(description = "Tipo de cuenta a crear", example = "CUENTA_AHORROS", required = true,
                allowableValues = {"CUENTA_AHORROS", "CUENTA_CORRIENTE"})
        @NotNull(message = "El tipo de cuenta es obligatorio")
        private TipoCuenta tipoCuenta;

        @Schema(description = "ID del cliente propietario de la cuenta", example = "1", required = true)
        @NotNull(message = "El ID del cliente es obligatorio")
        private Long clienteId;

        @Schema(description = "Saldo inicial de la cuenta", example = "100.00", required = true)
        @NotNull(message = "El saldo inicial es obligatorio")
        @DecimalMin(value = "0.0", message = "El saldo inicial no puede ser negativo")
        private BigDecimal saldoInicial;

        @Schema(description = "Indica si la cuenta está exenta del GMF", example = "false", required = true)
        @NotNull(message = "La exención GMF es obligatoria")
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

    @Schema(description = "Datos para cambiar el estado de un producto")
    public static class CambiarEstadoRequest {

        @Schema(description = "Nuevo estado del producto", example = "INACTIVA", required = true,
                allowableValues = {"ACTIVA", "INACTIVA", "CANCELADA"})
        @NotNull(message = "El nuevo estado es obligatorio")
        private EstadoCuenta nuevoEstado;

        public EstadoCuenta getNuevoEstado() { return nuevoEstado; }
        public void setNuevoEstado(EstadoCuenta nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    }

    @Schema(description = "Datos para actualizar el saldo de un producto")
    public static class ActualizarSaldoRequest {

        @Schema(description = "Nuevo saldo del producto", example = "250.00", required = true)
        @NotNull(message = "El nuevo saldo es obligatorio")
        @DecimalMin(value = "0.0", message = "El saldo no puede ser negativo para cuentas de ahorro")
        private BigDecimal nuevoSaldo;

        public BigDecimal getNuevoSaldo() { return nuevoSaldo; }
        public void setNuevoSaldo(BigDecimal nuevoSaldo) { this.nuevoSaldo = nuevoSaldo; }
    }

    @Schema(description = "Respuesta de validación de transacción")
    public static class TransaccionValidationResponse {

        @Schema(description = "Indica si se puede realizar la transacción", example = "true")
        private boolean puedeRealizar;

        public TransaccionValidationResponse(boolean puedeRealizar) {
            this.puedeRealizar = puedeRealizar;
        }

        public boolean isPuedeRealizar() { return puedeRealizar; }
        public void setPuedeRealizar(boolean puedeRealizar) { this.puedeRealizar = puedeRealizar; }
    }

    @Schema(description = "Respuesta de error estándar")
    public static class ErrorResponse {

        @Schema(description = "Mensaje de error", example = "Producto no encontrado con ID: 1")
        private String mensaje;

        public ErrorResponse(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}