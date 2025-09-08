package com.empresa.banking.infrastructure.controllers;

import com.empresa.banking.domain.entities.*;
import com.empresa.banking.domain.services.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ProductoController.class)
@DisplayName("Tests Unitarios - ProductoController")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto productoEjemplo;

    @BeforeEach
    void setUp() {
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
    @DisplayName("POST /api/productos - Crear producto exitoso")
    void crearProducto_Exitoso_RetornaCreated() throws Exception {
        // Arrange
        ProductoController.CrearProductoRequest request = new ProductoController.CrearProductoRequest();
        request.setTipoCuenta(TipoCuenta.CUENTA_AHORROS);
        request.setClienteId(1L);
        request.setSaldoInicial(BigDecimal.valueOf(1000));
        request.setExentaGmf(false);

        when(productoService.crearProducto(any(TipoCuenta.class), anyLong(), any(BigDecimal.class), anyBoolean()))
                .thenReturn(productoEjemplo);

        // Act & Assert
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tipoCuenta").value("CUENTA_AHORROS"))
                .andExpect(jsonPath("$.numeroCuenta").value("5312345678"))
                .andExpect(jsonPath("$.saldo").value(1000))
                .andExpect(jsonPath("$.clienteId").value(1L));

        verify(productoService).crearProducto(TipoCuenta.CUENTA_AHORROS, 1L, BigDecimal.valueOf(1000), false);
    }

    @Test
    @DisplayName("POST /api/productos - Cliente inexistente")
    void crearProducto_ClienteInexistente_RetornaBadRequest() throws Exception {
        // Arrange
        ProductoController.CrearProductoRequest request = new ProductoController.CrearProductoRequest();
        request.setTipoCuenta(TipoCuenta.CUENTA_AHORROS);
        request.setClienteId(999L);
        request.setSaldoInicial(BigDecimal.valueOf(1000));
        request.setExentaGmf(false);

        when(productoService.crearProducto(any(TipoCuenta.class), anyLong(), any(BigDecimal.class), anyBoolean()))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado con ID: 999"));

        verify(productoService).crearProducto(TipoCuenta.CUENTA_AHORROS, 999L, BigDecimal.valueOf(1000), false);
    }

    @Test
    @DisplayName("POST /api/productos - Error interno del servidor")
    void crearProducto_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        ProductoController.CrearProductoRequest request = new ProductoController.CrearProductoRequest();
        request.setTipoCuenta(TipoCuenta.CUENTA_AHORROS);
        request.setClienteId(1L);
        request.setSaldoInicial(BigDecimal.valueOf(1000));
        request.setExentaGmf(false);

        when(productoService.crearProducto(any(TipoCuenta.class), anyLong(), any(BigDecimal.class), anyBoolean()))
                .thenThrow(new RuntimeException("Error interno"));

        // Act & Assert
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));
    }

    // ========== TESTS BUSCAR PRODUCTO ==========

    @Test
    @DisplayName("GET /api/productos/{id} - Producto encontrado")
    void buscarProductoPorId_ProductoExiste_RetornaOk() throws Exception {
        // Arrange
        when(productoService.buscarProductoPorId(1L)).thenReturn(Optional.of(productoEjemplo));

        // Act & Assert
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroCuenta").value("5312345678"))
                .andExpect(jsonPath("$.saldo").value(1000));

        verify(productoService).buscarProductoPorId(1L);
    }

    @Test
    @DisplayName("GET /api/productos/{id} - Producto no encontrado")
    void buscarProductoPorId_ProductoNoExiste_RetornaNotFound() throws Exception {
        // Arrange
        when(productoService.buscarProductoPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound());

        verify(productoService).buscarProductoPorId(999L);
    }

    @Test
    @DisplayName("GET /api/productos/{id} - Error interno")
    void buscarProductoPorId_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        when(productoService.buscarProductoPorId(1L)).thenThrow(new RuntimeException("Error interno"));

        // Act & Assert
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));
    }

    @Test
    @DisplayName("GET /api/productos/numero-cuenta/{numeroCuenta} - Producto encontrado")
    void buscarProductoPorNumeroCuenta_ProductoExiste_RetornaOk() throws Exception {
        // Arrange
        when(productoService.buscarProductoPorNumeroCuenta("5312345678"))
                .thenReturn(Optional.of(productoEjemplo));

        // Act & Assert
        mockMvc.perform(get("/api/productos/numero-cuenta/5312345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroCuenta").value("5312345678"));

        verify(productoService).buscarProductoPorNumeroCuenta("5312345678");
    }

    @Test
    @DisplayName("GET /api/productos/numero-cuenta/{numeroCuenta} - Producto no encontrado")
    void buscarProductoPorNumeroCuenta_ProductoNoExiste_RetornaNotFound() throws Exception {
        // Arrange
        when(productoService.buscarProductoPorNumeroCuenta("9999999999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/productos/numero-cuenta/9999999999"))
                .andExpect(status().isNotFound());

        verify(productoService).buscarProductoPorNumeroCuenta("9999999999");
    }

    // ========== TESTS OBTENER PRODUCTOS ==========

    @Test
    @DisplayName("GET /api/productos - Obtener todos los productos")
    void obtenerTodosLosProductos_RetornaListaDeProductos() throws Exception {
        // Arrange
        List<Producto> productos = Arrays.asList(productoEjemplo);
        when(productoService.obtenerTodosLosProductos()).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].numeroCuenta").value("5312345678"));

        verify(productoService).obtenerTodosLosProductos();
    }

    @Test
    @DisplayName("GET /api/productos/cliente/{clienteId} - Obtener productos por cliente")
    void obtenerProductosPorCliente_RetornaProductosDelCliente() throws Exception {
        // Arrange
        List<Producto> productos = Arrays.asList(productoEjemplo);
        when(productoService.obtenerProductosPorCliente(1L)).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/productos/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(1L));

        verify(productoService).obtenerProductosPorCliente(1L);
    }

    // ========== TESTS CAMBIAR ESTADO ==========

    @Test
    @DisplayName("PUT /api/productos/{id}/estado - Cambiar estado exitoso")
    void cambiarEstadoProducto_Exitoso_RetornaOk() throws Exception {
        // Arrange
        ProductoController.CambiarEstadoRequest request = new ProductoController.CambiarEstadoRequest();
        request.setNuevoEstado(EstadoCuenta.INACTIVA);

        Producto productoInactivo = new Producto(1L, TipoCuenta.CUENTA_AHORROS, "5312345678",
                EstadoCuenta.INACTIVA, BigDecimal.valueOf(1000), false,
                LocalDateTime.now(), LocalDateTime.now(), 1L);

        when(productoService.cambiarEstadoProducto(1L, EstadoCuenta.INACTIVA))
                .thenReturn(productoInactivo);

        // Act & Assert
        mockMvc.perform(put("/api/productos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVA"));

        verify(productoService).cambiarEstadoProducto(1L, EstadoCuenta.INACTIVA);
    }

    @Test
    @DisplayName("PUT /api/productos/{id}/estado - Producto no encontrado")
    void cambiarEstadoProducto_ProductoNoEncontrado_RetornaBadRequest() throws Exception {
        // Arrange
        ProductoController.CambiarEstadoRequest request = new ProductoController.CambiarEstadoRequest();
        request.setNuevoEstado(EstadoCuenta.INACTIVA);

        when(productoService.cambiarEstadoProducto(999L, EstadoCuenta.INACTIVA))
                .thenThrow(new IllegalArgumentException("Producto no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/productos/999/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con ID: 999"));
    }

    @Test
    @DisplayName("PUT /api/productos/{id}/activar - Activar producto exitoso")
    void activarProducto_Exitoso_RetornaOk() throws Exception {
        // Arrange
        when(productoService.activarProducto(1L)).thenReturn(productoEjemplo);

        // Act & Assert
        mockMvc.perform(put("/api/productos/1/activar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        verify(productoService).activarProducto(1L);
    }

    @Test
    @DisplayName("PUT /api/productos/{id}/activar - Producto no encontrado")
    void activarProducto_ProductoNoEncontrado_RetornaBadRequest() throws Exception {
        // Arrange
        when(productoService.activarProducto(999L))
                .thenThrow(new IllegalArgumentException("Producto no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/productos/999/activar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con ID: 999"));
    }

    @Test
    @DisplayName("PUT /api/productos/{id}/inactivar - Inactivar producto exitoso")
    void inactivarProducto_Exitoso_RetornaOk() throws Exception {
        // Arrange
        Producto productoInactivo = new Producto(1L, TipoCuenta.CUENTA_AHORROS, "5312345678",
                EstadoCuenta.INACTIVA, BigDecimal.valueOf(1000), false,
                LocalDateTime.now(), LocalDateTime.now(), 1L);

        when(productoService.inactivarProducto(1L)).thenReturn(productoInactivo);

        // Act & Assert
        mockMvc.perform(put("/api/productos/1/inactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVA"));

        verify(productoService).inactivarProducto(1L);
    }

    @Test
    @DisplayName("PUT /api/productos/{id}/inactivar - Producto no encontrado")
    void inactivarProducto_ProductoNoEncontrado_RetornaBadRequest() throws Exception {
        // Arrange
        when(productoService.inactivarProducto(999L))
                .thenThrow(new IllegalArgumentException("Producto no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/productos/999/inactivar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con ID: 999"));
    }

    // ========== TESTS CANCELAR PRODUCTO ==========

    @Test
    @DisplayName("PUT /api/productos/{id}/cancelar - Cancelar producto exitoso")
    void cancelarProducto_SaldoCero_RetornaOk() throws Exception {
        // Arrange
        Producto productoCancelado = new Producto(1L, TipoCuenta.CUENTA_AHORROS, "5312345678",
                EstadoCuenta.CANCELADA, BigDecimal.ZERO, false,
                LocalDateTime.now(), LocalDateTime.now(), 1L);

        when(productoService.cancelarProducto(1L)).thenReturn(productoCancelado);

        // Act & Assert
        mockMvc.perform(put("/api/productos/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        verify(productoService).cancelarProducto(1L);
    }

    @Test
    @DisplayName("PUT /api/productos/{id}/cancelar - Producto con saldo")
    void cancelarProducto_ConSaldo_RetornaBadRequest() throws Exception {
        // Arrange
        when(productoService.cancelarProducto(1L))
                .thenThrow(new IllegalStateException("Solo se pueden cancelar productos con saldo cero"));

        // Act & Assert
        mockMvc.perform(put("/api/productos/1/cancelar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Solo se pueden cancelar productos con saldo cero"));

        verify(productoService).cancelarProducto(1L);
    }

    @Test
    @DisplayName("PUT /api/productos/{id}/cancelar - Producto no encontrado")
    void cancelarProducto_ProductoNoEncontrado_RetornaBadRequest() throws Exception {
        // Arrange
        when(productoService.cancelarProducto(999L))
                .thenThrow(new IllegalArgumentException("Producto no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/productos/999/cancelar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con ID: 999"));
    }

    // ========== TESTS ACTUALIZAR SALDO ==========

    @Test
    @DisplayName("PUT /api/productos/{id}/saldo - Actualizar saldo exitoso")
    void actualizarSaldo_Exitoso_RetornaOk() throws Exception {
        // Arrange
        ProductoController.ActualizarSaldoRequest request = new ProductoController.ActualizarSaldoRequest();
        request.setNuevoSaldo(BigDecimal.valueOf(2000));

        Producto productoActualizado = new Producto(1L, TipoCuenta.CUENTA_AHORROS, "5312345678",
                EstadoCuenta.ACTIVA, BigDecimal.valueOf(2000), false,
                LocalDateTime.now(), LocalDateTime.now(), 1L);

        when(productoService.actualizarSaldo(1L, BigDecimal.valueOf(2000)))
                .thenReturn(productoActualizado);

        // Act & Assert
        mockMvc.perform(put("/api/productos/1/saldo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(2000));

        verify(productoService).actualizarSaldo(1L, BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("PUT /api/productos/{id}/saldo - Producto no encontrado")
    void actualizarSaldo_ProductoNoEncontrado_RetornaBadRequest() throws Exception {
        // Arrange
        ProductoController.ActualizarSaldoRequest request = new ProductoController.ActualizarSaldoRequest();
        request.setNuevoSaldo(BigDecimal.valueOf(2000));

        when(productoService.actualizarSaldo(999L, BigDecimal.valueOf(2000)))
                .thenThrow(new IllegalArgumentException("Producto no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/productos/999/saldo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con ID: 999"));
    }

    // ========== TESTS VALIDACION TRANSACCION ==========

    @Test
    @DisplayName("GET /api/productos/{id}/puede-transaccion - Puede realizar transacción")
    void puedeRealizarTransaccion_Exitoso_RetornaTrue() throws Exception {
        // Arrange
        when(productoService.puedeRealizarTransaccion(1L, BigDecimal.valueOf(500), TipoTransaccion.RETIRO))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/productos/1/puede-transaccion")
                        .param("monto", "500")
                        .param("tipoTransaccion", "RETIRO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puedeRealizar").value(true));

        verify(productoService).puedeRealizarTransaccion(1L, BigDecimal.valueOf(500), TipoTransaccion.RETIRO);
    }

    @Test
    @DisplayName("GET /api/productos/{id}/puede-transaccion - No puede realizar transacción")
    void puedeRealizarTransaccion_NoPuede_RetornaFalse() throws Exception {
        // Arrange
        when(productoService.puedeRealizarTransaccion(1L, BigDecimal.valueOf(2000), TipoTransaccion.RETIRO))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/productos/1/puede-transaccion")
                        .param("monto", "2000")
                        .param("tipoTransaccion", "RETIRO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puedeRealizar").value(false));

        verify(productoService).puedeRealizarTransaccion(1L, BigDecimal.valueOf(2000), TipoTransaccion.RETIRO);
    }

    @Test
    @DisplayName("GET /api/productos/{id}/puede-transaccion - Producto no encontrado")
    void puedeRealizarTransaccion_ProductoNoEncontrado_RetornaBadRequest() throws Exception {
        // Arrange
        when(productoService.puedeRealizarTransaccion(999L, BigDecimal.valueOf(500), TipoTransaccion.RETIRO))
                .thenThrow(new IllegalArgumentException("Producto no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/productos/999/puede-transaccion")
                        .param("monto", "500")
                        .param("tipoTransaccion", "RETIRO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con ID: 999"));
    }

    // ========== TESTS ELIMINAR PRODUCTO ==========

    @Test
    @DisplayName("DELETE /api/productos/{id} - Eliminar producto exitoso")
    void eliminarProducto_Exitoso_RetornaNoContent() throws Exception {
        // Arrange
        doNothing().when(productoService).eliminarProducto(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());

        verify(productoService).eliminarProducto(1L);
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} - Producto con saldo")
    void eliminarProducto_ConSaldo_RetornaBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("No se puede eliminar un producto con saldo diferente a cero"))
                .when(productoService).eliminarProducto(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede eliminar un producto con saldo diferente a cero"));

        verify(productoService).eliminarProducto(1L);
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} - Producto inexistente")
    void eliminarProducto_ProductoInexistente_RetornaBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Producto no encontrado con ID: 999"))
                .when(productoService).eliminarProducto(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/productos/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con ID: 999"));

        verify(productoService).eliminarProducto(999L);
    }
}