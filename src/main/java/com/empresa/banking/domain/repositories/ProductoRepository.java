package com.empresa.banking.domain.repositories;

import com.empresa.banking.domain.entities.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    Optional<Producto> findById(Long id);
    List<Producto> findAll();
    Producto save(Producto producto);
    void deleteById(Long id);
    boolean existByNumeroCuenta(String numeroCuenta);
}
