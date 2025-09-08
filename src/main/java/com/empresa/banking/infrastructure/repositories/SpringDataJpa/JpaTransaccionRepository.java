package com.empresa.banking.infrastructure.repositories.SpringDataJpa;

import com.empresa.banking.infrastructure.entities.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaTransaccionRepository extends JpaRepository<TransaccionEntity, Long> {
    List<TransaccionEntity> findByCuentaOrigenId(Long cuentaOrigenId);
    void deleteByCuentaOrigenId(Long cuentaOrigenId);
}
