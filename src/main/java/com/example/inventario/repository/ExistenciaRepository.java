package com.example.inventario.repository;

import com.example.inventario.model.Existencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExistenciaRepository extends JpaRepository<Existencia, Long> {

    @EntityGraph(attributePaths = {"bodega", "producto"})
    List<Existencia> findAllByOrderByBodega_CodigoAscProducto_SkuAsc();

    @EntityGraph(attributePaths = {"producto"})
    List<Existencia> findByProducto_IdAndBodega_IdNotOrderByDisponibleDesc(Long productoId, Long bodegaId);

    @EntityGraph(attributePaths = {"bodega", "producto"})
    Optional<Existencia> findByBodega_IdAndProducto_Id(Long bodegaId, Long productoId);
}
