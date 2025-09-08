package com.empresa.banking.infrastructure.controllers;

import com.empresa.banking.domain.entities.Cliente;
import com.empresa.banking.domain.entities.TipoIdentificacion;
import com.empresa.banking.domain.services.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<?> crearCliente(@RequestBody CrearClienteRequest request) {
        try {
            Cliente cliente = clienteService.crearCliente(
                    request.getTipoIdentificacion(),
                    request.getNumeroIdentificacion(),
                    request.getNombres(),
                    request.getApellido(),
                    request.getCorreoElectronico(),
                    request.getFechaNacimiento()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarClientePorId(@PathVariable Long id) {
        try {
            Optional<Cliente> cliente = clienteService.buscarClientePorId(id);
            if (cliente.isPresent()) {
                return ResponseEntity.ok(cliente.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodosLosClientes() {
        try {
            List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(@PathVariable Long id,
                                               @RequestBody ActualizarClienteRequest request) {
        try {
            Cliente clienteActualizado = clienteService.actualizarCliente(
                    id,
                    request.getNombres(),
                    request.getApellido(),
                    request.getCorreoElectronico()
            );
            return ResponseEntity.ok(clienteActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCliente(@PathVariable Long id) {
        try {
            clienteService.eliminarCliente(id);
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

    @GetMapping("/{id}/existe")
    public ResponseEntity<?> validarExistenciaCliente(@PathVariable Long id) {
        try {
            Cliente cliente = clienteService.validarExistenciaCliente(id);
            return ResponseEntity.ok(cliente);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    public static class CrearClienteRequest {
        private TipoIdentificacion tipoIdentificacion;
        private String numeroIdentificacion;
        private String nombres;
        private String apellido;
        private String correoElectronico;
        private LocalDate fechaNacimiento;

        public TipoIdentificacion getTipoIdentificacion() { return tipoIdentificacion; }
        public void setTipoIdentificacion(TipoIdentificacion tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }
        public String getNumeroIdentificacion() { return numeroIdentificacion; }
        public void setNumeroIdentificacion(String numeroIdentificacion) { this.numeroIdentificacion = numeroIdentificacion; }
        public String getNombres() { return nombres; }
        public void setNombres(String nombres) { this.nombres = nombres; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getCorreoElectronico() { return correoElectronico; }
        public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    }

    public static class ActualizarClienteRequest {
        private String nombres;
        private String apellido;
        private String correoElectronico;

        public String getNombres() { return nombres; }
        public void setNombres(String nombres) { this.nombres = nombres; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getCorreoElectronico() { return correoElectronico; }
        public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
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