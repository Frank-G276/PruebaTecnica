package com.empresa.banking.domain.repositories;

import com.empresa.banking.domain.entities.Transaccion;

import java.util.List;
import java.util.Optional;

public interface TransaccionRepository {
    Optional<Transaccion> findById(Long id);
    List<Transaccion> findByAccountNumber(Long countNumber);
    List<Transaccion> findAll();
    Transaccion save(Transaccion transaccion);
    void deleteById(Long id);
    void deleteByAccountNumber(Long countNumber);

}
