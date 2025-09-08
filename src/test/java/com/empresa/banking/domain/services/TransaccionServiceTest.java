package com.empresa.banking.domain.services;

import com.empresa.banking.domain.entities.*;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - TransaccionService")
class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private TransaccionService transaccionService;

    private Producto cuentaActivaConSaldo;
    private Producto cuentaInactiva;
    private Producto cuentaDestino;
    private Transaccion transaccionEjemplo;

    @BeforeEach
    void setUp() {
        cuentaActivaConSaldo = new Producto(
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

        cuentaInactiva = new Producto(
                2L,
                TipoCuenta.CUENTA_CORRIENTE,
                "3312345678",
                EstadoCuenta.INACTIVA,
                BigDecimal.valueOf(500),
                false,
                LocalDateTime.now(),
                null,
                1L
        );

        cuentaDestino = new Producto(
                3L,
                TipoCuenta.CUENTA_AHORROS,
                "5398765432",
                EstadoCuenta.ACTIVA,
                BigDecimal.valueOf(2000),
                false,
                LocalDateTime.now(),
                null,
                2L
        );

        transaccionEjemplo = Transaccion.crear(
                TipoTransaccion.CONSIGNACION,
                BigDecimal.valueOf(100),
                1L,
                null,
                "Test consignación"
        );
    }

    // ========== TESTS REALIZAR CONSIGNACIÓN ==========

    @Test
    @DisplayName("Realizar consignación exitosa")
    void realizarConsignacion_CuentaActiva_RetornaTransaccionExitosa() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));
        when(productoRepository.save(any(Producto.class))).thenReturn(cuentaActivaConSaldo);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionEjemplo);

        // Act
        Transaccion resultado = transaccionService.realizarConsignacion(1L, BigDecimal.valueOf(100), "Test");

        // Assert
        assertNotNull(resultado);
        assertEquals(TipoTransaccion.CONSIGNACION, resultado.getTipoTransaccion());
        assertEquals(BigDecimal.valueOf(100), resultado.getMonto());
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(any(Producto.class));
        verify(transaccionRepository).save(any(Transaccion.class));
    }

    @Test
    @DisplayName("Realizar consignación en cuenta inactiva")
    void realizarConsignacion_CuentaInactiva_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(2L)).thenReturn(Optional.of(cuentaInactiva));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                transaccionService.realizarConsignacion(2L, BigDecimal.valueOf(100), "Test")
        );

        assertEquals("No se puede realizar transacciones en una cuenta inactiva", exception.getMessage());
        verify(productoRepository).findById(2L);
        verify(productoRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Realizar consignación con cuenta inexistente")
    void realizarConsignacion_CuentaInexistente_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transaccionService.realizarConsignacion(999L, BigDecimal.valueOf(100), "Test")
        );

        assertEquals("Cuenta no encontrada con ID: 999", exception.getMessage());
        verify(productoRepository).findById(999L);
        verify(productoRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Realizar consignación sin descripción")
    void realizarConsignacion_SinDescripcion_UsaDescripcionPorDefecto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));
        when(productoRepository.save(any(Producto.class))).thenReturn(cuentaActivaConSaldo);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionEjemplo);

        // Act
        Transaccion resultado = transaccionService.realizarConsignacion(1L, BigDecimal.valueOf(100), null);

        // Assert
        assertNotNull(resultado);
        verify(transaccionRepository).save(any(Transaccion.class));
    }

    // ========== TESTS REALIZAR RETIRO ==========

    @Test
    @DisplayName("Realizar retiro exitoso")
    void realizarRetiro_CuentaActivaConSaldo_RetornaTransaccionExitosa() {
        // Arrange
        Transaccion transaccionRetiro = Transaccion.crear(
                TipoTransaccion.RETIRO,  // Cambiar a RETIRO
                BigDecimal.valueOf(100),
                1L,
                null,
                "Test retiro"
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));
        when(productoRepository.save(any(Producto.class))).thenReturn(cuentaActivaConSaldo);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionRetiro);

        // Act
        Transaccion resultado = transaccionService.realizarRetiro(1L, BigDecimal.valueOf(100), "Test retiro");

        // Assert
        assertNotNull(resultado);
        assertEquals(TipoTransaccion.RETIRO, resultado.getTipoTransaccion());
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(any(Producto.class));
        verify(transaccionRepository).save(any(Transaccion.class));
    }

    @Test
    @DisplayName("Realizar retiro con fondos insuficientes")
    void realizarRetiro_FondosInsuficientes_LanzaExcepcion() {
        // Arrange
        Producto cuentaConPocoSaldo = new Producto(1L, TipoCuenta.CUENTA_AHORROS, "5312345678",
                EstadoCuenta.ACTIVA, BigDecimal.valueOf(50), false, LocalDateTime.now(), null, 1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaConPocoSaldo));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                transaccionService.realizarRetiro(1L, BigDecimal.valueOf(100), "Test")
        );

        assertEquals("No se puede realizar el retiro. Fondos insuficientes o cuenta inactiva", exception.getMessage());
        verify(productoRepository).findById(1L);
        verify(productoRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Realizar retiro en cuenta inactiva")
    void realizarRetiro_CuentaInactiva_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(2L)).thenReturn(Optional.of(cuentaInactiva));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                transaccionService.realizarRetiro(2L, BigDecimal.valueOf(100), "Test")
        );

        assertEquals("No se puede realizar el retiro. Fondos insuficientes o cuenta inactiva", exception.getMessage());
        verify(productoRepository).findById(2L);
        verify(productoRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Realizar retiro sin descripción")
    void realizarRetiro_SinDescripcion_UsaDescripcionPorDefecto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));
        when(productoRepository.save(any(Producto.class))).thenReturn(cuentaActivaConSaldo);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionEjemplo);

        // Act
        Transaccion resultado = transaccionService.realizarRetiro(1L, BigDecimal.valueOf(100), null);

        // Assert
        assertNotNull(resultado);
        verify(transaccionRepository).save(any(Transaccion.class));
    }

    // ========== TESTS REALIZAR TRANSFERENCIA ==========

    @Test
    @DisplayName("Realizar transferencia exitosa")
    void realizarTransferencia_CuentasActivasConSaldo_RetornaTransacciones() {
        // Arrange
        Transaccion transaccionDebito = Transaccion.crear(
                TipoTransaccion.TRANSFERENCIA,
                BigDecimal.valueOf(100),
                1L,
                3L,
                "Test"
        );

        Transaccion transaccionCredito = Transaccion.crear(
                TipoTransaccion.CONSIGNACION,
                BigDecimal.valueOf(100),
                3L,
                null,  // CONSIGNACION no puede tener cuenta destino
                "Transferencia recibida: Test"
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));
        when(productoRepository.findById(3L)).thenReturn(Optional.of(cuentaDestino));
        when(productoRepository.save(any(Producto.class))).thenReturn(cuentaActivaConSaldo);
        when(transaccionRepository.save(any(Transaccion.class)))
                .thenReturn(transaccionDebito)
                .thenReturn(transaccionCredito);

        // Act
        List<Transaccion> resultado = transaccionService.realizarTransferencia(1L, 3L, BigDecimal.valueOf(100), "Test");

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(productoRepository).findById(1L);
        verify(productoRepository).findById(3L);
        verify(productoRepository, times(2)).save(any(Producto.class));
        verify(transaccionRepository, times(2)).save(any(Transaccion.class));
    }

    @Test
    @DisplayName("Realizar transferencia con cuentas iguales")
    void realizarTransferencia_CuentasIguales_LanzaExcepcion() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transaccionService.realizarTransferencia(1L, 1L, BigDecimal.valueOf(100), "Test")
        );

        assertEquals("La cuenta origen y destino no pueden ser iguales", exception.getMessage());
        verify(productoRepository, never()).findById(any());
        verify(productoRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Realizar transferencia con cuenta origen inactiva")
    void realizarTransferencia_CuentaOrigenInactiva_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(2L)).thenReturn(Optional.of(cuentaInactiva));
        when(productoRepository.findById(3L)).thenReturn(Optional.of(cuentaDestino));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                transaccionService.realizarTransferencia(2L, 3L, BigDecimal.valueOf(100), "Test")
        );

        assertEquals("Ambas cuentas deben estar activas para realizar una transferencia", exception.getMessage());
        verify(productoRepository).findById(2L);
        verify(productoRepository).findById(3L);
        verify(productoRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Realizar transferencia con fondos insuficientes")
    void realizarTransferencia_FondosInsuficientes_LanzaExcepcion() {
        // Arrange
        Producto cuentaConPocoSaldo = new Producto(1L, TipoCuenta.CUENTA_AHORROS, "5312345678",
                EstadoCuenta.ACTIVA, BigDecimal.valueOf(50), false, LocalDateTime.now(), null, 1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaConPocoSaldo));
        when(productoRepository.findById(3L)).thenReturn(Optional.of(cuentaDestino));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                transaccionService.realizarTransferencia(1L, 3L, BigDecimal.valueOf(100), "Test")
        );

        assertEquals("Fondos insuficientes en la cuenta origen", exception.getMessage());
        verify(productoRepository).findById(1L);
        verify(productoRepository).findById(3L);
        verify(productoRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Realizar transferencia con cuenta destino inexistente")
    void realizarTransferencia_CuentaDestinoInexistente_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transaccionService.realizarTransferencia(1L, 999L, BigDecimal.valueOf(100), "Test")
        );

        assertEquals("Cuenta no encontrada con ID: 999", exception.getMessage());
        verify(productoRepository).findById(1L);
        verify(productoRepository).findById(999L);
        verify(productoRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Realizar transferencia sin descripción")
    void realizarTransferencia_SinDescripcion_UsaDescripcionPorDefecto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));
        when(productoRepository.findById(3L)).thenReturn(Optional.of(cuentaDestino));
        when(productoRepository.save(any(Producto.class))).thenReturn(cuentaActivaConSaldo);
        when(transaccionRepository.save(any(Transaccion.class)))
                .thenReturn(transaccionEjemplo)
                .thenReturn(transaccionEjemplo);

        // Act
        List<Transaccion> resultado = transaccionService.realizarTransferencia(1L, 3L, BigDecimal.valueOf(100), null);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(transaccionRepository, times(2)).save(any(Transaccion.class));
    }

    // ========== TESTS OBTENER HISTORIAL ==========

    @Test
    @DisplayName("Obtener historial de transacciones existente")
    void obtenerHistorialTransacciones_CuentaExiste_RetornaListaTransacciones() {
        // Arrange
        List<Transaccion> transacciones = Arrays.asList(transaccionEjemplo);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));
        when(transaccionRepository.findByAccountNumber(1L)).thenReturn(transacciones);

        // Act
        List<Transaccion> resultado = transaccionService.obtenerHistorialTransacciones(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(transaccionEjemplo, resultado.get(0));
        verify(productoRepository).findById(1L);
        verify(transaccionRepository).findByAccountNumber(1L);
    }

    @Test
    @DisplayName("Obtener historial de cuenta inexistente")
    void obtenerHistorialTransacciones_CuentaInexistente_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transaccionService.obtenerHistorialTransacciones(999L)
        );

        assertEquals("Cuenta no encontrada con ID: 999", exception.getMessage());
        verify(productoRepository).findById(999L);
        verify(transaccionRepository, never()).findByAccountNumber(any());
    }

    // ========== TESTS BUSCAR TRANSACCIÓN ==========

    @Test
    @DisplayName("Buscar transacción por ID existente")
    void buscarTransaccionPorId_TransaccionExiste_RetornaOptionalConTransaccion() {
        // Arrange
        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccionEjemplo));

        // Act
        Optional<Transaccion> resultado = transaccionService.buscarTransaccionPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(transaccionEjemplo, resultado.get());
        verify(transaccionRepository).findById(1L);
    }

    @Test
    @DisplayName("Buscar transacción por ID no existente")
    void buscarTransaccionPorId_TransaccionNoExiste_RetornaOptionalVacio() {
        // Arrange
        when(transaccionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Transaccion> resultado = transaccionService.buscarTransaccionPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(transaccionRepository).findById(999L);
    }

    // ========== TESTS OBTENER TODAS LAS TRANSACCIONES ==========

    @Test
    @DisplayName("Obtener todas las transacciones")
    void obtenerTodasLasTransacciones_RetornaListaCompleta() {
        // Arrange
        List<Transaccion> transacciones = Arrays.asList(transaccionEjemplo);
        when(transaccionRepository.findAll()).thenReturn(transacciones);

        // Act
        List<Transaccion> resultado = transaccionService.obtenerTodasLasTransacciones();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(transaccionEjemplo, resultado.get(0));
        verify(transaccionRepository).findAll();
    }

    // ========== TESTS CONSULTAR ESTADO DE CUENTA ==========

    @Test
    @DisplayName("Consultar estado de cuenta exitoso")
    void consultarEstadoCuenta_CuentaExiste_RetornaEstadoCuenta() {
        // Arrange
        List<Transaccion> transacciones = Arrays.asList(transaccionEjemplo);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));
        when(transaccionRepository.findByAccountNumber(1L)).thenReturn(transacciones);

        // Act
        TransaccionService.EstadoCuentaDto resultado = transaccionService.consultarEstadoCuenta(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getCuentaId());
        assertEquals("5312345678", resultado.getNumeroCuenta());
        assertEquals(TipoCuenta.CUENTA_AHORROS, resultado.getTipoCuenta());
        assertEquals(EstadoCuenta.ACTIVA, resultado.getEstado());
        assertEquals(BigDecimal.valueOf(1000), resultado.getSaldoActual());
        assertNotNull(resultado.getTransacciones());
        assertEquals(1, resultado.getTransacciones().size());
        verify(productoRepository).findById(1L);
        verify(transaccionRepository).findByAccountNumber(1L);
    }

    @Test
    @DisplayName("Consultar estado de cuenta inexistente")
    void consultarEstadoCuenta_CuentaInexistente_LanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transaccionService.consultarEstadoCuenta(999L)
        );

        assertEquals("Cuenta no encontrada con ID: 999", exception.getMessage());
        verify(productoRepository).findById(999L);
        verify(transaccionRepository, never()).findByAccountNumber(any());
    }

    // ========== TESTS ELIMINAR TRANSACCIÓN ==========

    @Test
    @DisplayName("Eliminar transacción existente")
    void eliminarTransaccion_TransaccionExiste_EliminaExitosamente() {
        // Arrange
        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccionEjemplo));

        // Act
        transaccionService.eliminarTransaccion(1L);

        // Assert
        verify(transaccionRepository).findById(1L);
        verify(transaccionRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar transacción inexistente")
    void eliminarTransaccion_TransaccionInexistente_LanzaExcepcion() {
        // Arrange
        when(transaccionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transaccionService.eliminarTransaccion(999L)
        );

        assertEquals("Transacción no encontrada con ID: 999", exception.getMessage());
        verify(transaccionRepository).findById(999L);
        verify(transaccionRepository, never()).deleteById(any());
    }

    // ========== TESTS VALIDAR CUENTA ==========

    @Test
    @DisplayName("Validar cuenta existente")
    void validarCuenta_CuentaExiste_RetornaCuenta() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cuentaActivaConSaldo));

        // Act & Assert
        // Este método es privado, se prueba indirectamente a través de otros métodos
        assertDoesNotThrow(() -> transaccionService.realizarConsignacion(1L, BigDecimal.valueOf(100), "Test"));
        verify(productoRepository).findById(1L);
    }
}