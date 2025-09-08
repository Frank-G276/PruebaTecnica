package com.empresa.banking.infrastructure.controllers;

import com.empresa.banking.domain.entities.Transaccion;
import com.empresa.banking.domain.services.TransaccionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = "*")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @PostMapping("/consignacion")
    public ResponseEntity<?> realizarConsignacion(@RequestBody ConsignacionRequest request) {
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

    @PostMapping("/retiro")
    public ResponseEntity<?> realizarRetiro(@RequestBody RetiroRequest request) {
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

    @PostMapping("/transferencia")
    public ResponseEntity<?> realizarTransferencia(@RequestBody TransferenciaRequest request) {
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

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarTransaccionPorId(@PathVariable Long id) {
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

    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<?> obtenerHistorialTransacciones(@PathVariable Long cuentaId) {
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

    @GetMapping("/estado-cuenta/{cuentaId}")
    public ResponseEntity<?> consultarEstadoCuenta(@PathVariable Long cuentaId) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarTransaccion(@PathVariable Long id) {
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

    public static class ConsignacionRequest {
        private Long cuentaId;
        private BigDecimal monto;
        private String descripcion;

        public Long getCuentaId() { return cuentaId; }
        public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    public static class RetiroRequest {
        private Long cuentaId;
        private BigDecimal monto;
        private String descripcion;

        public Long getCuentaId() { return cuentaId; }
        public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    public static class TransferenciaRequest {
        private Long cuentaOrigenId;
        private Long cuentaDestinoId;
        private BigDecimal monto;
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

    public static class ErrorResponse {
        private String mensaje;

        public ErrorResponse(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}