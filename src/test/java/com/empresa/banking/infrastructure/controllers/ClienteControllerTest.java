package com.empresa.banking.infrastructure.controllers;

import com.empresa.banking.domain.entities.Cliente;
import com.empresa.banking.domain.entities.Enums.TipoIdentificacion;
import com.empresa.banking.app.services.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - ClienteController")
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    private Cliente clienteEjemplo;

    @BeforeEach
    void setUp() {
        clienteEjemplo = new Cliente(
                1L,
                TipoIdentificacion.CEDULA_CIUDADANIA,
                "12345678",
                "Juan Carlos",
                "Pérez García",
                "juan.perez@email.com",
                LocalDate.of(1990, 5, 15),
                LocalDateTime.now(),
                null
        );
    }

    // ========== TESTS CREAR CLIENTE ==========

    @Test
    @DisplayName("Crear cliente exitoso")
    void crearCliente_Exitoso_RetornaCreated() {
        // Arrange
        ClienteController.CrearClienteRequest request = new ClienteController.CrearClienteRequest();
        request.setTipoIdentificacion(TipoIdentificacion.CEDULA_CIUDADANIA);
        request.setNumeroIdentificacion("12345678");
        request.setNombres("Juan Carlos");
        request.setApellido("Pérez García");
        request.setCorreoElectronico("juan.perez@email.com");
        request.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        when(clienteService.crearCliente(any(ClienteController.CrearClienteRequest.class)))
                .thenReturn(clienteEjemplo);

        // Act
        ResponseEntity<?> response = clienteController.crearCliente(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Cliente);
        Cliente cliente = (Cliente) response.getBody();
        assertEquals("Juan Carlos", cliente.getNombres());
        verify(clienteService).crearCliente(request);
    }

    @Test
    @DisplayName("Crear cliente con datos inválidos")
    void crearCliente_DatosInvalidos_RetornaBadRequest() {
        // Arrange
        ClienteController.CrearClienteRequest request = new ClienteController.CrearClienteRequest();
        request.setTipoIdentificacion(TipoIdentificacion.CEDULA_CIUDADANIA);
        request.setNumeroIdentificacion("12345678");
        request.setNombres("A"); // Muy corto
        request.setApellido("Pérez García");
        request.setCorreoElectronico("juan.perez@email.com");
        request.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        when(clienteService.crearCliente(any(ClienteController.CrearClienteRequest.class)))
                .thenThrow(new IllegalArgumentException("Los nombres deben tener al menos 2 caracteres"));

        // Act
        ResponseEntity<?> response = clienteController.crearCliente(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("Los nombres deben tener al menos 2 caracteres", error.getMensaje());
    }

    @Test
    @DisplayName("Crear cliente - error interno del servidor")
    void crearCliente_ErrorInterno_RetornaInternalServerError() {
        // Arrange
        ClienteController.CrearClienteRequest request = new ClienteController.CrearClienteRequest();
        request.setTipoIdentificacion(TipoIdentificacion.CEDULA_CIUDADANIA);
        request.setNumeroIdentificacion("12345678");
        request.setNombres("Juan Carlos");
        request.setApellido("Pérez García");
        request.setCorreoElectronico("juan.perez@email.com");
        request.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        when(clienteService.crearCliente(any(ClienteController.CrearClienteRequest.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        ResponseEntity<?> response = clienteController.crearCliente(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("Error interno del servidor", error.getMensaje());
    }

    // ========== TESTS BUSCAR POR ID ==========

    @Test
    @DisplayName("Buscar cliente por ID existente")
    void buscarClientePorId_ClienteExiste_RetornaOk() {
        // Arrange
        when(clienteService.buscarClientePorId(1L)).thenReturn(Optional.of(clienteEjemplo));

        // Act
        ResponseEntity<?> response = clienteController.buscarClientePorId(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(clienteEjemplo, response.getBody());
        verify(clienteService).buscarClientePorId(1L);
    }

    @Test
    @DisplayName("Buscar cliente por ID no existente")
    void buscarClientePorId_ClienteNoExiste_RetornaNotFound() {
        // Arrange
        when(clienteService.buscarClientePorId(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = clienteController.buscarClientePorId(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(clienteService).buscarClientePorId(1L);
    }

    @Test
    @DisplayName("Buscar cliente por ID - error interno")
    void buscarClientePorId_ErrorInterno_RetornaInternalServerError() {
        // Arrange
        when(clienteService.buscarClientePorId(1L))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        ResponseEntity<?> response = clienteController.buscarClientePorId(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("Error interno del servidor", error.getMensaje());
    }

    // ========== TESTS OBTENER TODOS ==========

    @Test
    @DisplayName("Obtener todos los clientes")
    void obtenerTodosLosClientes_RetornaListaDeClientes() {
        // Arrange
        List<Cliente> clientes = Arrays.asList(clienteEjemplo);
        when(clienteService.obtenerTodosLosClientes()).thenReturn(clientes);

        // Act
        ResponseEntity<?> response = clienteController.obtenerTodosLosClientes();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(clientes, response.getBody());
        verify(clienteService).obtenerTodosLosClientes();
    }

    @Test
    @DisplayName("Obtener todos los clientes - error interno")
    void obtenerTodosLosClientes_ErrorInterno_RetornaInternalServerError() {
        // Arrange
        when(clienteService.obtenerTodosLosClientes())
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        ResponseEntity<?> response = clienteController.obtenerTodosLosClientes();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("Error interno del servidor", error.getMensaje());
    }

    // ========== TESTS ACTUALIZAR CLIENTE ==========

    @Test
    @DisplayName("Actualizar cliente exitoso")
    void actualizarCliente_Exitoso_RetornaOk() {
        // Arrange
        ClienteController.ActualizarClienteRequest request = new ClienteController.ActualizarClienteRequest();
        request.setNombres("Juan Carlos Actualizado");
        request.setApellido("Pérez García");
        request.setCorreoElectronico("juan.actualizado@email.com");

        when(clienteService.actualizarCliente(eq(1L), any(ClienteController.ActualizarClienteRequest.class)))
                .thenReturn(clienteEjemplo);

        // Act
        ResponseEntity<?> response = clienteController.actualizarCliente(1L, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(clienteEjemplo, response.getBody());
        verify(clienteService).actualizarCliente(1L, request);
    }

    @Test
    @DisplayName("Actualizar cliente no existente")
    void actualizarCliente_ClienteNoExiste_RetornaBadRequest() {
        // Arrange
        ClienteController.ActualizarClienteRequest request = new ClienteController.ActualizarClienteRequest();
        request.setNombres("Juan Carlos");
        request.setApellido("Pérez García");
        request.setCorreoElectronico("juan.perez@email.com");

        when(clienteService.actualizarCliente(eq(1L), any(ClienteController.ActualizarClienteRequest.class)))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado con ID: 1"));

        // Act
        ResponseEntity<?> response = clienteController.actualizarCliente(1L, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("Cliente no encontrado con ID: 1", error.getMensaje());
    }

    @Test
    @DisplayName("Actualizar cliente - error interno")
    void actualizarCliente_ErrorInterno_RetornaInternalServerError() {
        // Arrange
        ClienteController.ActualizarClienteRequest request = new ClienteController.ActualizarClienteRequest();
        request.setNombres("Juan Carlos");
        request.setApellido("Pérez García");
        request.setCorreoElectronico("juan.perez@email.com");

        when(clienteService.actualizarCliente(eq(1L), any(ClienteController.ActualizarClienteRequest.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        ResponseEntity<?> response = clienteController.actualizarCliente(1L, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("Error interno del servidor", error.getMensaje());
    }

    // ========== TESTS ELIMINAR CLIENTE ==========

    @Test
    @DisplayName("Eliminar cliente exitoso")
    void eliminarCliente_Exitoso_RetornaNoContent() {
        // Arrange
        doNothing().when(clienteService).eliminarCliente(1L);

        // Act
        ResponseEntity<?> response = clienteController.eliminarCliente(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(clienteService).eliminarCliente(1L);
    }

    @Test
    @DisplayName("Eliminar cliente no existente")
    void eliminarCliente_ClienteNoExiste_RetornaBadRequest() {
        // Arrange
        doThrow(new IllegalArgumentException("Cliente no encontrado con ID: 1"))
                .when(clienteService).eliminarCliente(1L);

        // Act
        ResponseEntity<?> response = clienteController.eliminarCliente(1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("Cliente no encontrado con ID: 1", error.getMensaje());
    }

    @Test
    @DisplayName("Eliminar cliente con productos vinculados")
    void eliminarCliente_ConProductosVinculados_RetornaBadRequest() {
        // Arrange
        doThrow(new IllegalStateException("No se puede eliminar un cliente que tiene productos vinculados"))
                .when(clienteService).eliminarCliente(1L);

        // Act
        ResponseEntity<?> response = clienteController.eliminarCliente(1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("No se puede eliminar un cliente que tiene productos vinculados", error.getMensaje());
    }

    @Test
    @DisplayName("Eliminar cliente - error interno")
    void eliminarCliente_ErrorInterno_RetornaInternalServerError() {
        // Arrange
        doThrow(new RuntimeException("Error de base de datos"))
                .when(clienteService).eliminarCliente(1L);

        // Act
        ResponseEntity<?> response = clienteController.eliminarCliente(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("Error interno del servidor", error.getMensaje());
    }

    // ========== TESTS VALIDAR EXISTENCIA ==========

    @Test
    @DisplayName("Validar existencia de cliente")
    void validarExistenciaCliente_ClienteExiste_RetornaOk() {
        // Arrange
        when(clienteService.validarExistenciaCliente(1L)).thenReturn(clienteEjemplo);

        // Act
        ResponseEntity<?> response = clienteController.validarExistenciaCliente(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(clienteEjemplo, response.getBody());
        verify(clienteService).validarExistenciaCliente(1L);
    }

    @Test
    @DisplayName("Validar existencia de cliente no existente")
    void validarExistenciaCliente_ClienteNoExiste_RetornaNotFound() {
        // Arrange
        when(clienteService.validarExistenciaCliente(1L))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado con ID: 1"));

        // Act
        ResponseEntity<?> response = clienteController.validarExistenciaCliente(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(clienteService).validarExistenciaCliente(1L);
    }

    @Test
    @DisplayName("Validar existencia de cliente - error interno")
    void validarExistenciaCliente_ErrorInterno_RetornaInternalServerError() {
        // Arrange
        when(clienteService.validarExistenciaCliente(1L))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        ResponseEntity<?> response = clienteController.validarExistenciaCliente(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ClienteController.ErrorResponse error = (ClienteController.ErrorResponse) response.getBody();
        assertEquals("Error interno del servidor", error.getMensaje());
    }
}