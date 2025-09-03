package com.empresa.banking.infrastructure.repositories;

import com.empresa.banking.domain.entities.Transaccion;
import com.empresa.banking.domain.repositories.TransaccionRepository;
import com.empresa.banking.infrastructure.entities.TransaccionEntity;
import com.empresa.banking.infrastructure.mappers.Mappers;
import com.empresa.banking.infrastructure.repositories.SpringDataJpa.JpaTransaccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TransaccionRepositoryImpl implements TransaccionRepository {

    @Autowired
    private JpaTransaccionRepository jpaTransaccionRepository;
    @Autowired
    private Mappers mapper;

    @Override
    public Optional<Transaccion> findById(Long id){
        return jpaTransaccionRepository.findById(id).map(mapper::transaccionToDomain);
    }

    @Override
    public List<Transaccion> findByCountNumber(Long id){
        return jpaTransaccionRepository.findByAccountNumber(id).stream().map(mapper::transaccionToDomain).toList();
    }
    @Override
    public List<Transaccion> findAll(){
        return jpaTransaccionRepository.findAll().stream().map(mapper::transaccionToDomain).toList();
    }

    @Override
    public Transaccion save(Transaccion transaccion){
        TransaccionEntity entity = mapper.transaccionFromDomain(transaccion);
        return mapper.transaccionToDomain(jpaTransaccionRepository.save(entity));
    }

    @Override
    public void deleteById(Long id){
        jpaTransaccionRepository.deleteById(id);
    }

    @Override
    public void deleteByAccountNumber(Long id){
        jpaTransaccionRepository.deleteByAccountNumber(id);
    }
}
