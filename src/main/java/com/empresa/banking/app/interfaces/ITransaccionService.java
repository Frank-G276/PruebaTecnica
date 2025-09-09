package com.empresa.banking.app.interfaces;

import com.empresa.banking.domain.entities.Transaccion;
import com.empresa.banking.app.services.TransaccionService.EstadoCuentaDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ITransaccionService {

    Transaccion realizarConsignacion(Long cuentaId, BigDecimal monto, String descripcion);

    Transaccion realizarRetiro(Long cuentaId, BigDecimal monto, String descripcion);

    List<Transaccion> realizarTransferencia(Long cuentaOrigenId, Long cuentaDestinoId,
                                            BigDecimal monto, String descripcion);

    List<Transaccion> obtenerHistorialTransacciones(Long cuentaId);

    Optional<Transaccion> buscarTransaccionPorId(Long transaccionId);

    List<Transaccion> obtenerTodasLasTransacciones();

    EstadoCuentaDto consultarEstadoCuenta(Long cuentaId);

    void eliminarTransaccion(Long transaccionId);
}