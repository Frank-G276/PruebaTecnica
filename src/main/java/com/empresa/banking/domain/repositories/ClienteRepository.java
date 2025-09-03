package com.empresa.banking.domain.repositories;

import com.empresa.banking.domain.entities.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Optional<Cliente> findById(Long id);
    Cliente save(Cliente cliente);
    List<Cliente> findAll();
    void deleteById(Long id);

}
