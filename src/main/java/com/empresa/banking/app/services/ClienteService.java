package com.empresa.banking.app.services;

import com.empresa.banking.app.interfaces.IClienteService;
import com.empresa.banking.domain.entities.Cliente;
import com.empresa.banking.domain.repositories.ClienteRepository;
import com.empresa.banking.domain.repositories.ProductoRepository;
import com.empresa.banking.infrastructure.controllers.ClienteController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public ClienteService(ClienteRepository clienteRepository, ProductoRepository productoRepository) {
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Crea un nuevo cliente
     */
    public Cliente crearCliente(ClienteController.CrearClienteRequest request) {

        // Verificar si el cliente ya existe
        if (existeClientePorIdentificacion(request.getNumeroIdentificacion())) {
            throw new IllegalArgumentException("Ya existe un cliente con el número de identificación: " + request.getNumeroIdentificacion());
        }

        Cliente nuevoCliente = Cliente.crear(request.getTipoIdentificacion(), request.getNumeroIdentificacion(), request.getNombres(),request.getApellido(), request.getCorreoElectronico(),
                request.getFechaNacimiento());

        return clienteRepository.save(nuevoCliente);
    }

    /**
     * Busca un cliente por ID
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    /**
     * Obtiene todos los clientes
     */
    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    /**
     * Actualiza la información de un cliente
     */
    public Cliente actualizarCliente(Long id, ClienteController.ActualizarClienteRequest request) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        Cliente clienteActualizado = clienteExistente.actualizar(request.getNombres(), request.getApellido(), request.getCorreoElectronico());

        return clienteRepository.save(clienteActualizado);
    }

    /**
     * Elimina un cliente
     */
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        // Verificar si el cliente tiene productos vinculados
        if (tieneProductosVinculados(id)) {
            throw new IllegalStateException("No se puede eliminar un cliente que tiene productos vinculados");
        }

        clienteRepository.deleteById(id);
    }

    /**
     * Verifica si existe un cliente con el número de identificación dado
     */
    @Transactional(readOnly = true)
    public boolean existeClientePorIdentificacion(String numeroIdentificacion) {
        return clienteRepository.findAll().stream()
                .anyMatch(cliente -> cliente.getNumeroIdentificacion().equals(numeroIdentificacion));
    }

    /**
     * Verifica si un cliente tiene productos vinculados
     */
    @Transactional(readOnly = true)
    public boolean tieneProductosVinculados(Long clienteId) {
        return productoRepository.findAll().stream()
                .anyMatch(producto -> producto.getClienteId().equals(clienteId));
    }

    /**
     * Valida si un cliente existe y lo devuelve
     */
    @Transactional(readOnly = true)
    public Cliente validarExistenciaCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + clienteId));
    }
}