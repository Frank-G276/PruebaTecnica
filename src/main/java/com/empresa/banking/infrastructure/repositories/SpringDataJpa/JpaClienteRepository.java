package com.empresa.banking.infrastructure.repositories.SpringDataJpa;

import com.empresa.banking.infrastructure.entities.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaClienteRepository extends JpaRepository<ClienteEntity, Long> {
}
