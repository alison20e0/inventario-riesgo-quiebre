package com.example.recomendaciones.repository;

import com.example.recomendaciones.model.EstadoRecomendacion;
import com.example.recomendaciones.model.Recomendacion;
import com.example.recomendaciones.model.TipoRecomendacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {

    @EntityGraph(attributePaths = {"producto", "bodegaOrigen", "bodegaDestino"})
    List<Recomendacion> findAllByOrderByFechaCreacionDescIdDesc();

    @EntityGraph(attributePaths = {"producto", "bodegaOrigen", "bodegaDestino"})
    Optional<Recomendacion> findById(Long id);

    boolean existsByProducto_IdAndBodegaDestino_IdAndTipoAndEstado(
            Long productoId, Long bodegaDestinoId, TipoRecomendacion tipo, EstadoRecomendacion estado);
}
