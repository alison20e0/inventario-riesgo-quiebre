package com.example.inventario.repository;

import com.example.inventario.model.Bodega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodegaRepository extends JpaRepository<Bodega, Long> {

    Optional<Bodega> findByCodigo(String codigo);
}
