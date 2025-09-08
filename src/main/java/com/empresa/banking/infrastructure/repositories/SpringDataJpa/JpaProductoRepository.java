package com.empresa.banking.infrastructure.repositories.SpringDataJpa;

import com.empresa.banking.infrastructure.entities.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface JpaProductoRepository extends JpaRepository<ProductoEntity, Long> {
    boolean existsByNumeroCuenta(String numeroCuenta);
}
