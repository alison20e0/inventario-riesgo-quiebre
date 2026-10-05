package com.example.auditoria.repository;

import com.example.auditoria.model.Auditoria;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface AuditoriaRepository extends Repository<Auditoria, Long> {

    Auditoria save(Auditoria auditoria);

    List<Auditoria> findAllByOrderByFechaDescIdDesc();

    List<Auditoria> findByEntidadAndReferenciaIdOrderByFechaDescIdDesc(String entidad, Long referenciaId);
}
