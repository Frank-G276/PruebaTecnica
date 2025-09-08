package com.empresa.banking.domain.services;

import com.empresa.banking.domain.entities.*;
import com.empresa.banking.domain.repositories.ClienteRepository;
import com.empresa.banking.domain.repositories.ProductoRepository;
import com.empresa.banking.domain.repositories.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - ProductoService")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private ProductoService productoService;

    private Cliente clienteEjemplo;
    private Producto productoEjemplo;

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

        productoEjemplo = new Producto(
                1L,
                TipoCuenta.CUENTA_AHORROS,
                "5312345678",
                EstadoCuenta.ACTIVA,
                BigDecimal.valueOf(1000),
                false,
                LocalDateTime.now(),
                null,
                1L
        );
    }

    // ========== TESTS CREAR PRODUCTO ==========

    @Test
    @DisplayName("Crear producto exitoso")
    void crearProducto_Exitoso_RetornaProductoGuardado() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.existByNumeroCuenta(anyString())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        // Act
        Producto resultado = productoService.crearProducto(
                TipoCuenta.CUENTA_AHORROS, 1L, BigDecimal.valueOf(1000), false);

        // Assert
        assertNotNull(resultado);
        assertEquals(TipoCuenta.CUENTA_AHORROS, resultado.getTipoCuenta());
        assertEquals(1L, resultado.getClienteId());
        verify(clienteRepository).findById(1L);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("Crear producto con cliente inexistente")
    void crearProducto_ClienteInexistente_LanzaExcepcion() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                productoService.crearProducto(TipoCuenta.CUENTA_AHORROS, 999L, BigDecimal.ZERO, false)
        );

        assertEquals("Cliente no encontrado con ID: 999", exception.getMessage());
        verify(clienteRepository).findById(999L);
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear producto con número de cuenta duplicado")
    void crearProducto_NumeroCuentaDuplicado_GeneraNuevoNumero() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.existByNumeroCuenta(anyString()))
                .thenReturn(true)  // Primera vez existe
                .thenReturn(false); // Segunda vez no existe
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        // Act
        Producto resultado = productoService.crearProducto(
                TipoCuenta.CUENTA_AHORROS, 1L, BigDecimal.ZERO, false);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(2)).existByNumeroCuenta(anyString());
    }

    // ========== TESTS BUSCAR PRODUCTO ==========

    @Test
    @DisplayName("Buscar producto por ID existente")
    void buscarProductoPorId_ProductoExiste_RetornaOptionalConProducto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        // Act
        Optional<Producto> resultado = productoService.buscarProductoPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(productoEjemplo, resultado.get());
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Buscar producto por ID no existente")
    void buscarProductePorId_ProductoNoExiste_RetornaOptionalVacio() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Producto> resultado = productoService.buscarProductoPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(productoRepository).findById(999L);
    }

    @Test
    @DisplayName("Buscar producto por número de cuenta")
    void buscarProductoPorNumeroCuenta_ProductoExiste_RetornaOptionalConProducto() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Arrays.asList(productoEjemplo));

        // Act
        Optional<Producto> resultado = productoService.buscarProductoPorNumeroCuenta("5312345678");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(productoEjemplo, resultado.get());
        verify(productoRepository).findAll();
    }

    // ========== TESTS OBTENER PRODUCTOS ==========

    @Test
    @DisplayName("Obtener todos los productos")
    void obtenerTodosLosProductos_RetornaListaDeProductos() {
        // Arrange
        List<Producto> productos = Arrays.asList(productoEjemplo);
        when(productoRepository.findAll()).thenReturn(productos);

        // Act
        List<Producto> resultado = productoService.obtenerTodosLosProductos();

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(productoEjemplo, resultado.get(0));
        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("Obtener productos por cliente")
    void obtenerProductosPorCliente_RetornaProductosDelCliente() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Arrays.asList(productoEjemplo));

        // Act
        List<Producto> resultado = productoService.obtenerProductosPorCliente(1L);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(productoEjemplo, resultado.get(0));
        verify(productoRepository).findAll();
    }

    // ========== TESTS CAMBIAR ESTADO ==========

    @Test
    @DisplayName("Cambiar estado de producto exitoso")
    void cambiarEstadoProducto_ProductoExiste_RetornaProductoActualizado() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        // Act
        Producto resultado = productoService.cambiarEstadoProducto(1L, EstadoCuenta.INACTIVA);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("Cambiar estado de producto inexistente")
    void cambiarEstadoProducto_ProductoInexistente_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                productoService.cambiarEstadoProducto(999L, EstadoCuenta.INACTIVA)
        );

        assertEquals("Producto no encontrado con ID: 999", exception.getMessage());
        verify(productoRepository).findById(999L);
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Activar producto")
    void activarProducto_ProductoExiste_RetornaProductoActivado() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        // Act
        Producto resultado = productoService.activarProducto(1L);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("Inactivar producto")
    void inactivarProducto_ProductoExiste_RetornaProductoInactivado() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        // Act
        Producto resultado = productoService.inactivarProducto(1L);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(any(Producto.class));
    }

    // ========== TESTS CANCELAR PRODUCTO ==========

    @Test
    @DisplayName("Cancelar producto con saldo cero")
    void cancelarProducto_SaldoCero_CancelaExitosamente() {
        // Arrange
        Producto productoSaldoCero = new Producto(1L, TipoCuenta.CUENTA_AHORROS, "5312345678",
                EstadoCuenta.ACTIVA, BigDecimal.ZERO, false, LocalDateTime.now(), null, 1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoSaldoCero));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoSaldoCero);

        // Act
        Producto resultado = productoService.cancelarProducto(1L);

        // Assert - Cambiado a times(2)
        verify(productoRepository, times(2)).findById(1L);
        verify(transaccionRepository).deleteByAccountNumber(1L);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("Cancelar producto con saldo diferente a cero")
    void cancelarProducto_SaldoDiferenteCero_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                productoService.cancelarProducto(1L)
        );

        assertEquals("Solo se pueden cancelar productos con saldo cero", exception.getMessage());
        verify(productoRepository).findById(1L);
        verify(productoRepository, never()).save(any());
    }

    // ========== TESTS ACTUALIZAR SALDO ==========

    @Test
    @DisplayName("Actualizar saldo de producto")
    void actualizarSaldo_ProductoExiste_RetornaProductoConNuevoSaldo() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        // Act
        Producto resultado = productoService.actualizarSaldo(1L, BigDecimal.valueOf(2000));

        // Assert
        assertNotNull(resultado);
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(any(Producto.class));
    }

    // ========== TESTS VALIDACIONES ==========

    @Test
    @DisplayName("Verificar si puede realizar transacción")
    void puedeRealizarTransaccion_ProductoActivo_RetornaTrue() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        // Act
        boolean resultado = productoService.puedeRealizarTransaccion(1L, BigDecimal.valueOf(500), TipoTransaccion.RETIRO);

        // Assert
        assertTrue(resultado);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Eliminar producto con saldo cero")
    void eliminarProducto_SaldoCero_EliminaExitosamente() {
        // Arrange
        Producto productoSaldoCero = new Producto(1L, TipoCuenta.CUENTA_AHORROS, "5312345678",
                EstadoCuenta.ACTIVA, BigDecimal.ZERO, false, LocalDateTime.now(), null, 1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoSaldoCero));

        // Act
        productoService.eliminarProducto(1L);

        // Assert
        verify(productoRepository).findById(1L);
        verify(transaccionRepository).deleteByAccountNumber(1L);
        verify(productoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar producto con saldo diferente a cero")
    void eliminarProducto_SaldoDiferenteCero_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                productoService.eliminarProducto(1L)
        );

        assertEquals("No se puede eliminar un producto con saldo diferente a cero", exception.getMessage());
        verify(productoRepository).findById(1L);
        verify(productoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Validar existencia de producto")
    void validarExistenciaProducto_ProductoExiste_RetornaProducto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        // Act
        Producto resultado = productoService.validarExistenciaProducto(1L);

        // Assert
        assertEquals(productoEjemplo, resultado);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Validar existencia de producto inexistente")
    void validarExistenciaProducto_ProductoInexistente_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                productoService.validarExistenciaProducto(999L)
        );

        assertEquals("Producto no encontrado con ID: 999", exception.getMessage());
        verify(productoRepository).findById(999L);
    }

    @Test
    @DisplayName("Validar existencia por número de cuenta")
    void validarExistenciaProductoPorNumeroCuenta_ProductoExiste_RetornaProducto() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Arrays.asList(productoEjemplo));

        // Act
        Producto resultado = productoService.validarExistenciaProductoPorNumeroCuenta("5312345678");

        // Assert
        assertEquals(productoEjemplo, resultado);
        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("Validar existencia por número de cuenta inexistente")
    void validarExistenciaProductoPorNumeroCuenta_ProductoInexistente_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                productoService.validarExistenciaProductoPorNumeroCuenta("9999999999")
        );

        assertEquals("Producto no encontrado con número de cuenta: 9999999999", exception.getMessage());
        verify(productoRepository).findAll();
    }
}