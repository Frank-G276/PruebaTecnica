package com.empresa.banking.domain.services;

import com.empresa.banking.domain.entities.Cliente;
import com.empresa.banking.domain.entities.TipoIdentificacion;
import com.empresa.banking.domain.repositories.ClienteRepository;
import com.empresa.banking.domain.repositories.ProductoRepository;
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
    void crearCliente_Exitoso_RetornaClienteGuardado() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(Arrays.asList()); // No existe cliente con esa identificación
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteEjemplo);

        // Act
        Cliente resultado = clienteService.crearCliente(
                TipoIdentificacion.CEDULA_CIUDADANIA,
                "12345678",
                "Juan Carlos",
                "Pérez García",
                "juan.perez@email.com",
                LocalDate.of(1990, 5, 15)
        );

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
                clienteService.crearCliente(
                        TipoIdentificacion.CEDULA_CIUDADANIA,
                        "12345678",
                        "Juan Carlos",
                        "Pérez García",
                        "juan.perez@email.com",
                        LocalDate.of(1990, 5, 15)
                )
        );

        assertEquals("Ya existe un cliente con el número de identificación: 12345678", exception.getMessage());
        verify(clienteRepository).findAll();
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

    // ========== TESTS ACTUALIZAR CLIENTE ==========

    @Test
    @DisplayName("Actualizar cliente exitoso")
    void actualizarCliente_ClienteExiste_RetornaClienteActualizado() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteEjemplo);

        // Act
        Cliente resultado = clienteService.actualizarCliente(1L, "Juan Nuevo", "Apellido Nuevo", "nuevo@email.com");

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
                clienteService.actualizarCliente(1L, "Juan", "Pérez", "juan@email.com")
        );

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());
        verify(clienteRepository).findById(1L);
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