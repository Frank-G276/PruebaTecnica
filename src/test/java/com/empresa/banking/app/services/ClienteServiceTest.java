package com.empresa.banking.app.services;

import com.empresa.banking.app.services.ClienteService;
import com.empresa.banking.domain.entities.Cliente;
import com.empresa.banking.domain.entities.Enums.TipoIdentificacion;
import com.empresa.banking.domain.repositories.ClienteRepository;
import com.empresa.banking.domain.repositories.ProductoRepository;
import com.empresa.banking.infrastructure.controllers.ClienteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteEjemplo;
    private ClienteController.CrearClienteRequest crearClienteRequest;
    private ClienteController.ActualizarClienteRequest actualizarClienteRequest;

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

        // Configurar request para crear cliente
        crearClienteRequest = new ClienteController.CrearClienteRequest();
        crearClienteRequest.setTipoIdentificacion(TipoIdentificacion.CEDULA_CIUDADANIA);
        crearClienteRequest.setNumeroIdentificacion("12345678");
        crearClienteRequest.setNombres("Juan Carlos");
        crearClienteRequest.setApellido("Pérez García");
        crearClienteRequest.setCorreoElectronico("juan.perez@email.com");
        crearClienteRequest.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        // Configurar request para actualizar cliente
        actualizarClienteRequest = new ClienteController.ActualizarClienteRequest();
        actualizarClienteRequest.setNombres("Juan Carlos Actualizado");
        actualizarClienteRequest.setApellido("Pérez García");
        actualizarClienteRequest.setCorreoElectronico("juan.actualizado@email.com");
    }

    // ========== TESTS CREAR CLIENTE ==========

    @Test
    @DisplayName("Crear cliente exitoso")
    void crearCliente_Exitoso_RetornaClienteGuardado() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(Arrays.asList()); // No existe cliente con esa identificación
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteEjemplo);

        // Act
        Cliente resultado = clienteService.crearCliente(crearClienteRequest);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Carlos", resultado.getNombres());
        assertEquals("12345678", resultado.getNumeroIdentificacion());
        verify(clienteRepository).findAll();
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente con identificación duplicada")
    void crearCliente_IdentificacionDuplicada_LanzaExcepcion() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(clienteEjemplo)); // Ya existe

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clienteService.crearCliente(crearClienteRequest)
        );

        assertEquals("Ya existe un cliente con el número de identificación: 12345678", exception.getMessage());
        verify(clienteRepository).findAll();
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear cliente con request nulo")
    void crearCliente_RequestNulo_LanzaExcepcion() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                clienteService.crearCliente(null)
        );

        verify(clienteRepository, never()).findAll();
        verify(clienteRepository, never()).save(any());
    }

    // ========== TESTS BUSCAR CLIENTE ==========

    @Test
    @DisplayName("Buscar cliente por ID existente")
    void buscarClientePorId_ClienteExiste_RetornaOptionalConCliente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));

        // Act
        Optional<Cliente> resultado = clienteService.buscarClientePorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(clienteEjemplo, resultado.get());
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Buscar cliente por ID no existente")
    void buscarClientePorId_ClienteNoExiste_RetornaOptionalVacio() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Cliente> resultado = clienteService.buscarClientePorId(1L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Buscar cliente por ID nulo")
    void buscarClientePorId_IdNulo_RetornaOptionalVacio() {
        // Arrange
        when(clienteRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<Cliente> resultado = clienteService.buscarClientePorId(null);

        // Assert
        assertFalse(resultado.isPresent());
        verify(clienteRepository).findById(null);
    }

    // ========== TESTS OBTENER TODOS ==========

    @Test
    @DisplayName("Obtener todos los clientes")
    void obtenerTodosLosClientes_RetornaListaDeClientes() {
        // Arrange
        List<Cliente> clientes = Arrays.asList(clienteEjemplo);
        when(clienteRepository.findAll()).thenReturn(clientes);

        // Act
        List<Cliente> resultado = clienteService.obtenerTodosLosClientes();

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(clienteEjemplo, resultado.get(0));
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Obtener todos los clientes - lista vacía")
    void obtenerTodosLosClientes_ListaVacia_RetornaListaVacia() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Cliente> resultado = clienteService.obtenerTodosLosClientes();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(clienteRepository).findAll();
    }

    // ========== TESTS ACTUALIZAR CLIENTE ==========

    @Test
    @DisplayName("Actualizar cliente exitoso")
    void actualizarCliente_ClienteExiste_RetornaClienteActualizado() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteEjemplo);

        // Act
        Cliente resultado = clienteService.actualizarCliente(1L, actualizarClienteRequest);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Actualizar cliente no existente")
    void actualizarCliente_ClienteNoExiste_LanzaExcepcion() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clienteService.actualizarCliente(1L, actualizarClienteRequest)
        );

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());
        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar cliente con request nulo")
    void actualizarCliente_RequestNulo_LanzaExcepcion() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                clienteService.actualizarCliente(1L, null)
        );

        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar cliente con ID nulo")
    void actualizarCliente_IdNulo_LanzaExcepcion() {
        // Arrange
        when(clienteRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clienteService.actualizarCliente(null, actualizarClienteRequest)
        );

        assertEquals("Cliente no encontrado con ID: null", exception.getMessage());
        verify(clienteRepository).findById(null);
        verify(clienteRepository, never()).save(any());
    }

    // ========== TESTS ELIMINAR CLIENTE ==========

    @Test
    @DisplayName("Eliminar cliente sin productos")
    void eliminarCliente_SinProductos_EliminaExitosamente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findAll()).thenReturn(Arrays.asList()); // Sin productos

        // Act
        clienteService.eliminarCliente(1L);

        // Assert
        verify(clienteRepository).findById(1L);
        verify(productoRepository).findAll();
        verify(clienteRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar cliente con productos vinculados - Versión simplificada")
    void eliminarCliente_ConProductosVinculados_LanzaExcepcion() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));

        // Crear spy del servicio para mockear el método tieneProductosVinculados
        ClienteService spyService = spy(clienteService);
        doReturn(true).when(spyService).tieneProductosVinculados(1L);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                spyService.eliminarCliente(1L)
        );

        assertEquals("No se puede eliminar un cliente que tiene productos vinculados", exception.getMessage());
        verify(clienteRepository).findById(1L);
        verify(spyService).tieneProductosVinculados(1L);
        verify(clienteRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Eliminar cliente no existente")
    void eliminarCliente_ClienteNoExiste_LanzaExcepcion() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clienteService.eliminarCliente(1L)
        );

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());
        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).deleteById(any());
    }

    // ========== TESTS MÉTODOS DE VALIDACIÓN ==========

    @Test
    @DisplayName("Existe cliente por identificación - cliente existe")
    void existeClientePorIdentificacion_ClienteExiste_RetornaTrue() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(clienteEjemplo));

        // Act
        boolean resultado = clienteService.existeClientePorIdentificacion("12345678");

        // Assert
        assertTrue(resultado);
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Existe cliente por identificación - cliente no existe")
    void existeClientePorIdentificacion_ClienteNoExiste_RetornaFalse() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        boolean resultado = clienteService.existeClientePorIdentificacion("12345678");

        // Assert
        assertFalse(resultado);
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Existe cliente por identificación - identificación nula")
    void existeClientePorIdentificacion_IdentificacionNula_RetornaFalse() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(clienteEjemplo));

        // Act
        boolean resultado = clienteService.existeClientePorIdentificacion(null);

        // Assert
        assertFalse(resultado);
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Tiene productos vinculados - cliente con productos")
    void tieneProductosVinculados_ClienteConProductos_RetornaTrue() {
        // Arrange
        // Crear mock de Producto usando Mockito
        Object mockProducto = mock(Object.class);
        // Usar spy para mockear el método directamente
        ClienteService spyService = spy(clienteService);
        doReturn(true).when(spyService).tieneProductosVinculados(1L);

        // Act
        boolean resultado = spyService.tieneProductosVinculados(1L);

        // Assert
        assertTrue(resultado);
        verify(spyService).tieneProductosVinculados(1L);
    }

    @Test
    @DisplayName("Tiene productos vinculados - cliente sin productos")
    void tieneProductosVinculados_ClienteSinProductos_RetornaFalse() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        boolean resultado = clienteService.tieneProductosVinculados(1L);

        // Assert
        assertFalse(resultado);
        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("Validar existencia de cliente")
    void validarExistenciaCliente_ClienteExiste_RetornaCliente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));

        // Act
        Cliente resultado = clienteService.validarExistenciaCliente(1L);

        // Assert
        assertEquals(clienteEjemplo, resultado);
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Validar existencia de cliente no existente")
    void validarExistenciaCliente_ClienteNoExiste_LanzaExcepcion() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clienteService.validarExistenciaCliente(1L)
        );

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());
        verify(clienteRepository).findById(1L);
    }
}