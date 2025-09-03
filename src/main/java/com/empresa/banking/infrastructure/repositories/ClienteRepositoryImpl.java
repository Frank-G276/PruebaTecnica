package com.empresa.banking.infrastructure.repositories;

import com.empresa.banking.domain.entities.Cliente;
import com.empresa.banking.domain.repositories.ClienteRepository;
import com.empresa.banking.infrastructure.entities.ClienteEntity;
import com.empresa.banking.infrastructure.mappers.Mappers;
import com.empresa.banking.infrastructure.repositories.SpringDataJpa.JpaClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClienteRepositoryImpl implements ClienteRepository {

    @Autowired
    private JpaClienteRepository jpaClienteRepository;
    @Autowired
    private Mappers mappers;

    @Override
    public Optional<Cliente> findById(Long id){
        return jpaClienteRepository.findById(id).map(mappers::clienteToDomain);
    }

    @Override
    public List<Cliente> findAll() {
        return jpaClienteRepository.findAll()
                .stream()
                .map(mappers::clienteToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Cliente save(Cliente cliente){
        ClienteEntity entity = mappers.clienteFromDomain(cliente);
        return mappers.clienteToDomain(jpaClienteRepository.save(entity));
    }

    @Override
    public void deleteById(Long id){
        jpaClienteRepository.deleteById(id);
    }


}
