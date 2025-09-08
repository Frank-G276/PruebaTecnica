package com.empresa.banking.infrastructure.controllers;

import com.empresa.banking.domain.entities.Cliente;
import com.empresa.banking.domain.entities.TipoIdentificacion;
import com.empresa.banking.domain.services.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
@Tag(name = "Clientes", description = "API para gestión de clientes bancarios")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(
            summary = "Crear un nuevo cliente",
            description = "Registra un nuevo cliente en el sistema bancario. El cliente debe ser mayor de edad."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Cliente.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o cliente menor de edad",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<?> crearCliente(
            @Valid @RequestBody CrearClienteRequest request) {
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

    @Operation(
            summary = "Buscar cliente por ID",
            description = "Obtiene la información de un cliente específico por su identificador único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Cliente.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarClientePorId(
            @Parameter(description = "ID único del cliente", required = true)
            @PathVariable Long id) {
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

    @Operation(
            summary = "Obtener todos los clientes",
            description = "Retorna la lista completa de clientes registrados en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de clientes obtenida exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Cliente.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
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

    @Operation(
            summary = "Actualizar información del cliente",
            description = "Permite modificar los datos básicos de un cliente existente (nombres, apellido y correo)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Cliente.class))
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
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(
            @Parameter(description = "ID único del cliente", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActualizarClienteRequest request) {
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

    @Operation(
            summary = "Eliminar cliente",
            description = "Elimina un cliente del sistema. No se puede eliminar si tiene productos vinculados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Cliente eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede eliminar cliente con productos vinculados o cliente no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCliente(
            @Parameter(description = "ID único del cliente", required = true)
            @PathVariable Long id) {
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

    @Operation(
            summary = "Validar existencia del cliente",
            description = "Verifica si existe un cliente con el ID proporcionado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Cliente.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}/existe")
    public ResponseEntity<?> validarExistenciaCliente(
            @Parameter(description = "ID único del cliente", required = true)
            @PathVariable Long id) {
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

    @Schema(description = "Datos requeridos para crear un nuevo cliente")
    public static class CrearClienteRequest {

        @Schema(description = "Tipo de identificación del cliente", example = "CEDULA_CIUDADANIA", required = true)
        @NotNull(message = "El tipo de identificación es obligatorio")
        private TipoIdentificacion tipoIdentificacion;

        @Schema(description = "Número de identificación del cliente", example = "12345678", required = true)
        @NotBlank(message = "El número de identificación es obligatorio")
        private String numeroIdentificacion;

        @Schema(description = "Nombres del cliente", example = "Juan Carlos", required = true)
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(min = 2, message = "Los nombres deben tener al menos 2 caracteres")
        private String nombres;

        @Schema(description = "Apellido del cliente", example = "Pérez García", required = true)
        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, message = "El apellido debe tener al menos 2 caracteres")
        private String apellido;

        @Schema(description = "Correo electrónico del cliente", example = "juan.perez@email.com", required = true)
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Debe ser un correo electrónico válido")
        private String correoElectronico;

        @Schema(description = "Fecha de nacimiento del cliente", example = "1990-05-15", required = true)
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        private LocalDate fechaNacimiento;

        // Getters y Setters
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

    @Schema(description = "Datos para actualizar un cliente existente")
    public static class ActualizarClienteRequest {

        @Schema(description = "Nombres actualizados del cliente", example = "Juan Carlos")
        @Size(min = 2, message = "Los nombres deben tener al menos 2 caracteres")
        private String nombres;

        @Schema(description = "Apellido actualizado del cliente", example = "Pérez García")
        @Size(min = 2, message = "El apellido debe tener al menos 2 caracteres")
        private String apellido;

        @Schema(description = "Correo electrónico actualizado del cliente", example = "juan.nuevo@email.com")
        @Email(message = "Debe ser un correo electrónico válido")
        private String correoElectronico;

        public String getNombres() { return nombres; }
        public void setNombres(String nombres) { this.nombres = nombres; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getCorreoElectronico() { return correoElectronico; }
        public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    }

    @Schema(description = "Respuesta de error estándar")
    public static class ErrorResponse {

        @Schema(description = "Mensaje de error", example = "Los nombres deben tener al menos 2 caracteres")
        private String mensaje;

        public ErrorResponse(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}