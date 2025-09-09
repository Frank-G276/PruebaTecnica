package com.empresa.banking.app.interfaces;

import com.empresa.banking.domain.entities.Cliente;
import com.empresa.banking.infrastructure.controllers.ClienteController;

import java.util.List;
import java.util.Optional;

public interface IClienteService {

    /**
     * Crea un nuevo cliente
     */
    Cliente crearCliente(ClienteController.CrearClienteRequest request);

    /**
     * Busca un cliente por ID
     */
    Optional<Cliente> buscarClientePorId(Long id);

    /**
     * Obtiene todos los clientes
     */
    List<Cliente> obtenerTodosLosClientes();

    /**
     * Actualiza la información de un cliente
     */
    Cliente actualizarCliente(Long id, ClienteController.ActualizarClienteRequest request);

    /**
     * Elimina un cliente
     */
    void eliminarCliente(Long id);

    /**
     * Verifica si existe un cliente con el número de identificación dado
     */
    boolean existeClientePorIdentificacion(String numeroIdentificacion);

    /**
     * Verifica si un cliente tiene productos vinculados
     */
    boolean tieneProductosVinculados(Long clienteId);

    /**
     * Valida si un cliente existe y lo devuelve
     */
    Cliente validarExistenciaCliente(Long clienteId);
}
