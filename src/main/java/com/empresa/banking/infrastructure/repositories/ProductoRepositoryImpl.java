package com.empresa.banking.infrastructure.repositories;

import com.empresa.banking.domain.repositories.ProductoRepository;

import com.empresa.banking.domain.entities.Producto;
import com.empresa.banking.infrastructure.entities.ProductoEntity;
import com.empresa.banking.infrastructure.mappers.Mappers;
import com.empresa.banking.infrastructure.repositories.SpringDataJpa.JpaProductoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductoRepositoryImpl implements ProductoRepository {

    @Autowired
    private JpaProductoRepository jpaRepository;

    @Autowired
    private Mappers mappers;


    @Override
    public Optional<Producto> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mappers::productoToDomain);
    }

    @Override
    public List<Producto> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mappers::productoToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Producto save(Producto producto) {
        ProductoEntity entity = mappers.productoFromDomain(producto);
        return mappers.productoToDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existByNumeroCuenta(String numeroCuenta) {
        return jpaRepository.existByNumeroCuenta(numeroCuenta);
    }
}
