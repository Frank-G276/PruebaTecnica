package com.empresa.banking.infrastructure.mappers;

import com.empresa.banking.domain.entities.Cliente;
import com.empresa.banking.domain.entities.Producto;
import com.empresa.banking.domain.entities.Transaccion;
import com.empresa.banking.infrastructure.entities.ClienteEntity;
import com.empresa.banking.infrastructure.entities.ProductoEntity;
import com.empresa.banking.infrastructure.entities.TransaccionEntity;
import com.empresa.banking.infrastructure.repositories.SpringDataJpa.JpaClienteRepository;
import com.empresa.banking.infrastructure.repositories.SpringDataJpa.JpaProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Mappers {
    private final JpaClienteRepository jpaClienteRepository;
    private final JpaProductoRepository jpaProductoRepository;

    public Mappers(JpaClienteRepository jpaClienteRepository, JpaProductoRepository jpaProductoRepository) {
        this.jpaClienteRepository = jpaClienteRepository;
        this.jpaProductoRepository = jpaProductoRepository;
    }

    public Producto productoToDomain(ProductoEntity entity){
        return new Producto(
                entity.getId(),
                entity.getTipoCuenta(),
                entity.getNumeroCuenta(),
                entity.getEstado(),
                entity.getSaldo(),
                entity.getExentaGmf(),
                entity.getFechaCreacion(),
                entity.getFechaModificacion(),
                entity.getCliente().getId()
        );
    }

    public ProductoEntity productoFromDomain(Producto producto){
        Optional<ClienteEntity> cliente = jpaClienteRepository.findById(producto.getClienteId());
        ClienteEntity clienteEntity = cliente.orElseThrow(() ->
                new EntityNotFoundException("Cliente no encontrado"));

        ProductoEntity entity = new ProductoEntity();
        entity.setId(producto.getId());
        entity.setTipoCuenta(producto.getTipoCuenta());
        entity.setNumeroCuenta(producto.getNumeroCuenta());
        entity.setEstado(producto.getEstado());
        entity.setSaldo(producto.getSaldo());
        entity.setExentaGmf(producto.getExentaGmf());
        entity.setFechaCreacion(producto.getFechaCreacion());
        entity.setFechaModificacion(producto.getFechaModificacion());
        entity.setCliente(clienteEntity);
        return entity;
    }

    public ClienteEntity clienteFromDomain(Cliente cliente) {
        ClienteEntity entity = new ClienteEntity();
        entity.setId(cliente.getId());
        entity.setTipoIdentificacion(cliente.getTipoIdentificacion());
        entity.setNumeroIdentificacion(cliente.getNumeroIdentificacion());
        entity.setNombres(cliente.getNombres());
        entity.setApellido(cliente.getApellido());
        entity.setCorreoElectronico(cliente.getCorreoElectronico());
        entity.setFechaNacimiento(cliente.getFechaNacimiento());
        entity.setFechaCreacion(cliente.getFechaCreacion());
        entity.setFechaModificacion(cliente.getFechaModificacion());
        return entity;
    }

    public Cliente clienteToDomain(ClienteEntity clienteEntity) {
        return new Cliente(
                clienteEntity.getId(),
                clienteEntity.getTipoIdentificacion(),
                clienteEntity.getNumeroIdentificacion(),
                clienteEntity.getNombres(),
                clienteEntity.getApellido(),
                clienteEntity.getCorreoElectronico(),
                clienteEntity.getFechaNacimiento(),
                clienteEntity.getFechaCreacion(),
                clienteEntity.getFechaModificacion()
                );
    }

    public Transaccion transaccionToDomain(TransaccionEntity transaccion){
        Long cuentaDestinoId = null;
        if (transaccion.getCuentaDestino() != null) {
            cuentaDestinoId = transaccion.getCuentaDestino().getId();
        }
        return new Transaccion(
                transaccion.getId(),
                transaccion.getTipoTransaccion(),
                transaccion.getMonto(),
                transaccion.getDescripcion(),
                transaccion.getFechaTransaccion(),
                transaccion.getCuentaOrigen().getId(),
                cuentaDestinoId,
                transaccion.getSaldoAnterior(),
                transaccion.getSaldoActual()
        );
    }

    public TransaccionEntity transaccionFromDomain(Transaccion transaccion){
        // Cuenta Origen (NUNCA debe ser null)
        ProductoEntity cuentaOrigen = jpaProductoRepository.findById(transaccion.getCuentaOrigenId())
                .orElseThrow(() -> new EntityNotFoundException("Cuenta origen no encontrada con ID: " + transaccion.getCuentaOrigenId()));

        // Cuenta Destino (PUEDE ser null)
        ProductoEntity cuentaDestino = null;
        if (transaccion.getCuentaDestinoId() != null) {
            cuentaDestino = jpaProductoRepository.findById(transaccion.getCuentaDestinoId())
                    .orElseThrow(() -> new EntityNotFoundException("Cuenta destino no encontrada con ID: " + transaccion.getCuentaDestinoId()));
        }

        TransaccionEntity entity = new TransaccionEntity();
        entity.setId(transaccion.getId());
        entity.setTipoTransaccion(transaccion.getTipoTransaccion());
        entity.setMonto(transaccion.getMonto());
        entity.setDescripcion(transaccion.getDescripcion());
        entity.setFechaTransaccion(transaccion.getFechaTransaccion());
        entity.setCuentaOrigen(cuentaOrigen);
        entity.setCuentaDestino(cuentaDestino);
        entity.setSaldoAnterior(transaccion.getSaldoAnterior());
        entity.setSaldoActual(transaccion.getSaldoActual());
        return entity;
    }
}
