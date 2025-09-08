package com.empresa.banking.infrastructure.controllers;

import com.empresa.banking.domain.entities.*;
import com.empresa.banking.domain.services.TransaccionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
@DisplayName("Tests Unitarios - TransaccionController")
class TransaccionControllerTest {

    @Mock
    private TransaccionService transaccionService;

    @InjectMocks
    private TransaccionController transaccionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Transaccion transaccionEjemplo;
    private TransaccionService.EstadoCuentaDto estadoCuentaEjemplo;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transaccionController).build();
        objectMapper = new ObjectMapper();

        transaccionEjemplo = Transaccion.crear(
                TipoTransaccion.CONSIGNACION,
                BigDecimal.valueOf(100),
                1L,
                null,
                "Test consignación"
        );

        estadoCuentaEjemplo = new TransaccionService.EstadoCuentaDto(
                1L,
                "5312345678",
                TipoCuenta.CUENTA_AHORROS,
                EstadoCuenta.ACTIVA,
                BigDecimal.valueOf(1000),
                LocalDateTime.now(),
                Arrays.asList(transaccionEjemplo)
        );
    }

    // ========== TESTS REALIZAR CONSIGNACIÓN ==========

    @Test
    @DisplayName("Realizar consignación exitosa")
    void realizarConsignacion_DatosValidos_RetornaCreated() throws Exception {
        // Arrange
        TransaccionController.ConsignacionRequest request = new TransaccionController.ConsignacionRequest();
        request.setCuentaId(1L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test consignación");

        when(transaccionService.realizarConsignacion(1L, BigDecimal.valueOf(100), "Test consignación"))
                .thenReturn(transaccionEjemplo);

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/consignacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransaccion").value("CONSIGNACION"))
                .andExpect(jsonPath("$.monto").value(100))
                .andExpect(jsonPath("$.cuentaOrigenId").value(1));

        verify(transaccionService).realizarConsignacion(1L, BigDecimal.valueOf(100), "Test consignación");
    }

    @Test
    @DisplayName("Realizar consignación con datos inválidos")
    void realizarConsignacion_DatosInvalidos_RetornaBadRequest() throws Exception {
        // Arrange
        TransaccionController.ConsignacionRequest request = new TransaccionController.ConsignacionRequest();
        request.setCuentaId(999L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test");

        when(transaccionService.realizarConsignacion(999L, BigDecimal.valueOf(100), "Test"))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/consignacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Cuenta no encontrada con ID: 999"));

        verify(transaccionService).realizarConsignacion(999L, BigDecimal.valueOf(100), "Test");
    }

    @Test
    @DisplayName("Realizar consignación con cuenta inactiva")
    void realizarConsignacion_CuentaInactiva_RetornaBadRequest() throws Exception {
        // Arrange
        TransaccionController.ConsignacionRequest request = new TransaccionController.ConsignacionRequest();
        request.setCuentaId(1L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test");

        when(transaccionService.realizarConsignacion(1L, BigDecimal.valueOf(100), "Test"))
                .thenThrow(new IllegalStateException("No se puede realizar transacciones en una cuenta inactiva"));

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/consignacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede realizar transacciones en una cuenta inactiva"));

        verify(transaccionService).realizarConsignacion(1L, BigDecimal.valueOf(100), "Test");
    }

    @Test
    @DisplayName("Realizar consignación con error interno")
    void realizarConsignacion_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        TransaccionController.ConsignacionRequest request = new TransaccionController.ConsignacionRequest();
        request.setCuentaId(1L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test");

        when(transaccionService.realizarConsignacion(1L, BigDecimal.valueOf(100), "Test"))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/consignacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));

        verify(transaccionService).realizarConsignacion(1L, BigDecimal.valueOf(100), "Test");
    }

    // ========== TESTS REALIZAR RETIRO ==========

    @Test
    @DisplayName("Realizar retiro exitoso")
    void realizarRetiro_DatosValidos_RetornaCreated() throws Exception {
        // Arrange
        TransaccionController.RetiroRequest request = new TransaccionController.RetiroRequest();
        request.setCuentaId(1L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test retiro");

        Transaccion transaccionRetiro = Transaccion.crear(
                TipoTransaccion.RETIRO,
                BigDecimal.valueOf(100),
                1L,
                null,
                "Test retiro"
        );

        when(transaccionService.realizarRetiro(1L, BigDecimal.valueOf(100), "Test retiro"))
                .thenReturn(transaccionRetiro);

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransaccion").value("RETIRO"))
                .andExpect(jsonPath("$.monto").value(100))
                .andExpect(jsonPath("$.cuentaOrigenId").value(1));

        verify(transaccionService).realizarRetiro(1L, BigDecimal.valueOf(100), "Test retiro");
    }

    @Test
    @DisplayName("Realizar retiro con fondos insuficientes")
    void realizarRetiro_FondosInsuficientes_RetornaBadRequest() throws Exception {
        // Arrange
        TransaccionController.RetiroRequest request = new TransaccionController.RetiroRequest();
        request.setCuentaId(1L);
        request.setMonto(BigDecimal.valueOf(2000));
        request.setDescripcion("Test");

        when(transaccionService.realizarRetiro(1L, BigDecimal.valueOf(2000), "Test"))
                .thenThrow(new IllegalStateException("No se puede realizar el retiro. Fondos insuficientes o cuenta inactiva"));

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede realizar el retiro. Fondos insuficientes o cuenta inactiva"));

        verify(transaccionService).realizarRetiro(1L, BigDecimal.valueOf(2000), "Test");
    }

    @Test
    @DisplayName("Realizar retiro con cuenta inexistente")
    void realizarRetiro_CuentaInexistente_RetornaBadRequest() throws Exception {
        // Arrange
        TransaccionController.RetiroRequest request = new TransaccionController.RetiroRequest();
        request.setCuentaId(999L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test");

        when(transaccionService.realizarRetiro(999L, BigDecimal.valueOf(100), "Test"))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Cuenta no encontrada con ID: 999"));

        verify(transaccionService).realizarRetiro(999L, BigDecimal.valueOf(100), "Test");
    }

    @Test
    @DisplayName("Realizar retiro con error interno")
    void realizarRetiro_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        TransaccionController.RetiroRequest request = new TransaccionController.RetiroRequest();
        request.setCuentaId(1L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test");

        when(transaccionService.realizarRetiro(1L, BigDecimal.valueOf(100), "Test"))
                .thenThrow(new RuntimeException("Error de conexión"));

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));

        verify(transaccionService).realizarRetiro(1L, BigDecimal.valueOf(100), "Test");
    }

    // ========== TESTS REALIZAR TRANSFERENCIA ==========

    @Test
    @DisplayName("Realizar transferencia exitosa")
    void realizarTransferencia_DatosValidos_RetornaCreated() throws Exception {
        // Arrange
        TransaccionController.TransferenciaRequest request = new TransaccionController.TransferenciaRequest();
        request.setCuentaOrigenId(1L);
        request.setCuentaDestinoId(2L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test transferencia");

        // Crear transacción de débito (cuenta origen) - TRANSFERENCIA con cuenta destino
        Transaccion transaccionDebito = Transaccion.crear(
                TipoTransaccion.TRANSFERENCIA,
                BigDecimal.valueOf(100),
                1L,
                2L,  // Esta SÍ puede tener cuenta destino porque es TRANSFERENCIA
                "Test transferencia"
        );

        // Crear transacción de crédito (cuenta destino) - CONSIGNACION sin cuenta destino
        Transaccion transaccionCredito = Transaccion.crear(
                TipoTransaccion.CONSIGNACION,
                BigDecimal.valueOf(100),
                2L,
                null,  // CONSIGNACION no puede tener cuenta destino
                "Transferencia recibida: Test transferencia"
        );

        List<Transaccion> transacciones = Arrays.asList(transaccionDebito, transaccionCredito);

        when(transaccionService.realizarTransferencia(1L, 2L, BigDecimal.valueOf(100), "Test transferencia"))
                .thenReturn(transacciones);

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(transaccionService).realizarTransferencia(1L, 2L, BigDecimal.valueOf(100), "Test transferencia");
    }
    @Test
    @DisplayName("Realizar transferencia con cuentas iguales")
    void realizarTransferencia_CuentasIguales_RetornaBadRequest() throws Exception {
        // Arrange
        TransaccionController.TransferenciaRequest request = new TransaccionController.TransferenciaRequest();
        request.setCuentaOrigenId(1L);
        request.setCuentaDestinoId(1L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test");

        when(transaccionService.realizarTransferencia(1L, 1L, BigDecimal.valueOf(100), "Test"))
                .thenThrow(new IllegalArgumentException("La cuenta origen y destino no pueden ser iguales"));

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La cuenta origen y destino no pueden ser iguales"));

        verify(transaccionService).realizarTransferencia(1L, 1L, BigDecimal.valueOf(100), "Test");
    }

    @Test
    @DisplayName("Realizar transferencia con fondos insuficientes")
    void realizarTransferencia_FondosInsuficientes_RetornaBadRequest() throws Exception {
        // Arrange
        TransaccionController.TransferenciaRequest request = new TransaccionController.TransferenciaRequest();
        request.setCuentaOrigenId(1L);
        request.setCuentaDestinoId(2L);
        request.setMonto(BigDecimal.valueOf(2000));
        request.setDescripcion("Test");

        when(transaccionService.realizarTransferencia(1L, 2L, BigDecimal.valueOf(2000), "Test"))
                .thenThrow(new IllegalStateException("Fondos insuficientes en la cuenta origen"));

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Fondos insuficientes en la cuenta origen"));

        verify(transaccionService).realizarTransferencia(1L, 2L, BigDecimal.valueOf(2000), "Test");
    }

    @Test
    @DisplayName("Realizar transferencia con error interno")
    void realizarTransferencia_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        TransaccionController.TransferenciaRequest request = new TransaccionController.TransferenciaRequest();
        request.setCuentaOrigenId(1L);
        request.setCuentaDestinoId(2L);
        request.setMonto(BigDecimal.valueOf(100));
        request.setDescripcion("Test");

        when(transaccionService.realizarTransferencia(1L, 2L, BigDecimal.valueOf(100), "Test"))
                .thenThrow(new RuntimeException("Error de conexión"));

        // Act & Assert
        mockMvc.perform(post("/api/transacciones/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));

        verify(transaccionService).realizarTransferencia(1L, 2L, BigDecimal.valueOf(100), "Test");
    }

    // ========== TESTS BUSCAR TRANSACCIÓN POR ID ==========

    @Test
    @DisplayName("Buscar transacción por ID existente")
    void buscarTransaccionPorId_TransaccionExiste_RetornaOk() throws Exception {
        // Arrange
        when(transaccionService.buscarTransaccionPorId(1L)).thenReturn(Optional.of(transaccionEjemplo));

        // Act & Assert
        mockMvc.perform(get("/api/transacciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoTransaccion").value("CONSIGNACION"))
                .andExpect(jsonPath("$.monto").value(100))
                .andExpect(jsonPath("$.cuentaOrigenId").value(1));

        verify(transaccionService).buscarTransaccionPorId(1L);
    }

    @Test
    @DisplayName("Buscar transacción por ID no existente")
    void buscarTransaccionPorId_TransaccionNoExiste_RetornaNotFound() throws Exception {
        // Arrange
        when(transaccionService.buscarTransaccionPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/transacciones/999"))
                .andExpect(status().isNotFound());

        verify(transaccionService).buscarTransaccionPorId(999L);
    }

    @Test
    @DisplayName("Buscar transacción con error interno")
    void buscarTransaccionPorId_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        when(transaccionService.buscarTransaccionPorId(1L)).thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/transacciones/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));

        verify(transaccionService).buscarTransaccionPorId(1L);
    }

    // ========== TESTS OBTENER TODAS LAS TRANSACCIONES ==========

    @Test
    @DisplayName("Obtener todas las transacciones exitoso")
    void obtenerTodasLasTransacciones_Exitoso_RetornaOk() throws Exception {
        // Arrange
        List<Transaccion> transacciones = Arrays.asList(transaccionEjemplo);
        when(transaccionService.obtenerTodasLasTransacciones()).thenReturn(transacciones);

        // Act & Assert
        mockMvc.perform(get("/api/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipoTransaccion").value("CONSIGNACION"));

        verify(transaccionService).obtenerTodasLasTransacciones();
    }

    @Test
    @DisplayName("Obtener todas las transacciones con error interno")
    void obtenerTodasLasTransacciones_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        when(transaccionService.obtenerTodasLasTransacciones()).thenThrow(new RuntimeException("Error de conexión"));

        // Act & Assert
        mockMvc.perform(get("/api/transacciones"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));

        verify(transaccionService).obtenerTodasLasTransacciones();
    }

    // ========== TESTS OBTENER HISTORIAL TRANSACCIONES ==========

    @Test
    @DisplayName("Obtener historial de transacciones exitoso")
    void obtenerHistorialTransacciones_CuentaExiste_RetornaOk() throws Exception {
        // Arrange
        List<Transaccion> transacciones = Arrays.asList(transaccionEjemplo);
        when(transaccionService.obtenerHistorialTransacciones(1L)).thenReturn(transacciones);

        // Act & Assert
        mockMvc.perform(get("/api/transacciones/cuenta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipoTransaccion").value("CONSIGNACION"));

        verify(transaccionService).obtenerHistorialTransacciones(1L);
    }

    @Test
    @DisplayName("Obtener historial de cuenta inexistente")
    void obtenerHistorialTransacciones_CuentaInexistente_RetornaBadRequest() throws Exception {
        // Arrange
        when(transaccionService.obtenerHistorialTransacciones(999L))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/transacciones/cuenta/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Cuenta no encontrada con ID: 999"));

        verify(transaccionService).obtenerHistorialTransacciones(999L);
    }

    @Test
    @DisplayName("Obtener historial con error interno")
    void obtenerHistorialTransacciones_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        when(transaccionService.obtenerHistorialTransacciones(1L)).thenThrow(new RuntimeException("Error de conexión"));

        // Act & Assert
        mockMvc.perform(get("/api/transacciones/cuenta/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));

        verify(transaccionService).obtenerHistorialTransacciones(1L);
    }

    // ========== TESTS CONSULTAR ESTADO DE CUENTA ==========

    @Test
    @DisplayName("Consultar estado de cuenta exitoso")
    void consultarEstadoCuenta_CuentaExiste_RetornaOk() throws Exception {
        // Arrange
        when(transaccionService.consultarEstadoCuenta(1L)).thenReturn(estadoCuentaEjemplo);

        // Act & Assert
        mockMvc.perform(get("/api/transacciones/estado-cuenta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaId").value(1))
                .andExpect(jsonPath("$.numeroCuenta").value("5312345678"))
                .andExpect(jsonPath("$.tipoCuenta").value("CUENTA_AHORROS"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.saldoActual").value(1000))
                .andExpect(jsonPath("$.transacciones").isArray());

        verify(transaccionService).consultarEstadoCuenta(1L);
    }

    @Test
    @DisplayName("Consultar estado de cuenta inexistente")
    void consultarEstadoCuenta_CuentaInexistente_RetornaBadRequest() throws Exception {
        // Arrange
        when(transaccionService.consultarEstadoCuenta(999L))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/transacciones/estado-cuenta/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Cuenta no encontrada con ID: 999"));

        verify(transaccionService).consultarEstadoCuenta(999L);
    }

    @Test
    @DisplayName("Consultar estado de cuenta con error interno")
    void consultarEstadoCuenta_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        when(transaccionService.consultarEstadoCuenta(1L)).thenThrow(new RuntimeException("Error de conexión"));

        // Act & Assert
        mockMvc.perform(get("/api/transacciones/estado-cuenta/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));

        verify(transaccionService).consultarEstadoCuenta(1L);
    }

    // ========== TESTS ELIMINAR TRANSACCIÓN ==========

    @Test
    @DisplayName("Eliminar transacción exitosa")
    void eliminarTransaccion_TransaccionExiste_RetornaNoContent() throws Exception {
        // Arrange
        doNothing().when(transaccionService).eliminarTransaccion(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/transacciones/1"))
                .andExpect(status().isNoContent());

        verify(transaccionService).eliminarTransaccion(1L);
    }

    @Test
    @DisplayName("Eliminar transacción inexistente")
    void eliminarTransaccion_TransaccionInexistente_RetornaBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Transacción no encontrada con ID: 999"))
                .when(transaccionService).eliminarTransaccion(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/transacciones/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Transacción no encontrada con ID: 999"));

        verify(transaccionService).eliminarTransaccion(999L);
    }

    @Test
    @DisplayName("Eliminar transacción con error interno")
    void eliminarTransaccion_ErrorInterno_RetornaInternalServerError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error de conexión")).when(transaccionService).eliminarTransaccion(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/transacciones/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));

        verify(transaccionService).eliminarTransaccion(1L);
    }
}