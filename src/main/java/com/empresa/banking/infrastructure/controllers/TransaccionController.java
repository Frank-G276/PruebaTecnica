package com.empresa.banking.infrastructure.controllers;

import com.empresa.banking.domain.entities.Transaccion;
import com.empresa.banking.domain.services.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = "*")
@Tag(name = "Transacciones", description = "API para gestión de transacciones bancarias (consignaciones, retiros y transferencias)")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @Operation(
            summary = "Realizar consignación",
            description = "Realiza un depósito de dinero en una cuenta bancaria. La cuenta debe estar activa para permitir la transacción."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Consignación realizada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaccion.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuenta no encontrada, inactiva o datos inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/consignacion")
    public ResponseEntity<?> realizarConsignacion(@Valid @RequestBody ConsignacionRequest request) {
        try {
            Transaccion transaccion = transaccionService.realizarConsignacion(
                    request.getCuentaId(),
                    request.getMonto(),
                    request.getDescripcion()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
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
            summary = "Realizar retiro",
            description = "Realiza un retiro de dinero de una cuenta bancaria. " +
                    "Se valida que la cuenta esté activa y tenga fondos suficientes. " +
                    "Las cuentas de ahorro no pueden quedar con saldo negativo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Retiro realizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaccion.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Fondos insuficientes, cuenta inactiva o no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/retiro")
    public ResponseEntity<?> realizarRetiro(@Valid @RequestBody RetiroRequest request) {
        try {
            Transaccion transaccion = transaccionService.realizarRetiro(
                    request.getCuentaId(),
                    request.getMonto(),
                    request.getDescripcion()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
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
            summary = "Realizar transferencia",
            description = "Realiza una transferencia de dinero entre dos cuentas del sistema. " +
                    "Se genera un movimiento de débito en la cuenta origen y un crédito en la cuenta destino. " +
                    "Ambas cuentas deben existir y estar activas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Transferencia realizada exitosamente. Retorna ambas transacciones generadas.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaccion.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuentas iguales, fondos insuficientes, cuentas inactivas o no encontradas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/transferencia")
    public ResponseEntity<?> realizarTransferencia(@Valid @RequestBody TransferenciaRequest request) {
        try {
            List<Transaccion> transacciones = transaccionService.realizarTransferencia(
                    request.getCuentaOrigenId(),
                    request.getCuentaDestinoId(),
                    request.getMonto(),
                    request.getDescripcion()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transacciones);
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
            summary = "Buscar transacción por ID",
            description = "Obtiene la información detallada de una transacción específica por su identificador único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transacción encontrada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaccion.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transacción no encontrada"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarTransaccionPorId(
            @Parameter(description = "ID único de la transacción", required = true)
            @PathVariable Long id) {
        try {
            Optional<Transaccion> transaccion = transaccionService.buscarTransaccionPorId(id);
            if (transaccion.isPresent()) {
                return ResponseEntity.ok(transaccion.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @Operation(
            summary = "Obtener todas las transacciones",
            description = "Retorna la lista completa de transacciones registradas en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de transacciones obtenida exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaccion.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<?> obtenerTodasLasTransacciones() {
        try {
            List<Transaccion> transacciones = transaccionService.obtenerTodasLasTransacciones();
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @Operation(
            summary = "Obtener historial de transacciones de una cuenta",
            description = "Retorna todas las transacciones realizadas en una cuenta específica, ordenadas cronológicamente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Historial de transacciones obtenido exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaccion.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuenta no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<?> obtenerHistorialTransacciones(
            @Parameter(description = "ID de la cuenta para consultar el historial", required = true)
            @PathVariable Long cuentaId) {
        try {
            List<Transaccion> transacciones = transaccionService.obtenerHistorialTransacciones(cuentaId);
            return ResponseEntity.ok(transacciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @Operation(
            summary = "Consultar estado de cuenta",
            description = "Genera un estado de cuenta completo con información de la cuenta y su historial de transacciones. " +
                    "Incluye datos como saldo actual, tipo de cuenta, estado y lista de movimientos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de cuenta generado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransaccionService.EstadoCuentaDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuenta no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/estado-cuenta/{cuentaId}")
    public ResponseEntity<?> consultarEstadoCuenta(
            @Parameter(description = "ID de la cuenta para generar el estado de cuenta", required = true)
            @PathVariable Long cuentaId) {
        try {
            TransaccionService.EstadoCuentaDto estadoCuenta = transaccionService.consultarEstadoCuenta(cuentaId);
            return ResponseEntity.ok(estadoCuenta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @Operation(
            summary = "Eliminar transacción",
            description = "Elimina una transacción del sistema. Esta operación debe usarse con precaución ya que puede afectar la integridad contable."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Transacción eliminada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Transacción no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarTransaccion(
            @Parameter(description = "ID único de la transacción a eliminar", required = true)
            @PathVariable Long id) {
        try {
            transaccionService.eliminarTransaccion(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @Schema(description = "Datos requeridos para realizar una consignación")
    public static class ConsignacionRequest {

        @Schema(description = "ID de la cuenta donde se realizará la consignación", example = "1", required = true)
        @NotNull(message = "El ID de la cuenta es obligatorio")
        private Long cuentaId;

        @Schema(description = "Monto a consignar", example = "100.00", required = true)
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        private BigDecimal monto;

        @Schema(description = "Descripción o concepto de la consignación", example = "Depósito inicial")
        private String descripcion;

        public Long getCuentaId() { return cuentaId; }
        public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    @Schema(description = "Datos requeridos para realizar un retiro")
    public static class RetiroRequest {

        @Schema(description = "ID de la cuenta de donde se realizará el retiro", example = "1", required = true)
        @NotNull(message = "El ID de la cuenta es obligatorio")
        private Long cuentaId;

        @Schema(description = "Monto a retirar", example = "50.00", required = true)
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        private BigDecimal monto;

        @Schema(description = "Descripción o concepto del retiro", example = "Retiro en cajero automático")
        private String descripcion;

        public Long getCuentaId() { return cuentaId; }
        public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    @Schema(description = "Datos requeridos para realizar una transferencia entre cuentas")
    public static class TransferenciaRequest {

        @Schema(description = "ID de la cuenta origen (de donde sale el dinero)", example = "1", required = true)
        @NotNull(message = "El ID de la cuenta origen es obligatorio")
        private Long cuentaOrigenId;

        @Schema(description = "ID de la cuenta destino (donde llega el dinero)", example = "2", required = true)
        @NotNull(message = "El ID de la cuenta destino es obligatorio")
        private Long cuentaDestinoId;

        @Schema(description = "Monto a transferir", example = "75.00", required = true)
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        private BigDecimal monto;

        @Schema(description = "Descripción o concepto de la transferencia", example = "Transferencia a cuenta de ahorros")
        private String descripcion;

        public Long getCuentaOrigenId() { return cuentaOrigenId; }
        public void setCuentaOrigenId(Long cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId; }
        public Long getCuentaDestinoId() { return cuentaDestinoId; }
        public void setCuentaDestinoId(Long cuentaDestinoId) { this.cuentaDestinoId = cuentaDestinoId; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    @Schema(description = "Respuesta de error estándar")
    public static class ErrorResponse {

        @Schema(description = "Mensaje de error", example = "Fondos insuficientes para realizar el retiro")
        private String mensaje;

        public ErrorResponse(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}