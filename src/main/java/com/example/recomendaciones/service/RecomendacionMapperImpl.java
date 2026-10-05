package com.example.recomendaciones.service;

import com.example.recomendaciones.dto.RecomendacionResponse;
import com.example.inventario.model.Bodega;
import com.example.recomendaciones.model.Recomendacion;
import org.springframework.stereotype.Component;

@Component
public class RecomendacionMapperImpl implements RecomendacionMapper {

    @Override
    public RecomendacionResponse toResponse(Recomendacion recomendacion) {
        Bodega origen = recomendacion.getBodegaOrigen();
        Bodega destino = recomendacion.getBodegaDestino();
        return new RecomendacionResponse(
                recomendacion.getId(),
                recomendacion.getTipo(),
                recomendacion.getEstado(),
                recomendacion.getRiesgo(),
                recomendacion.getOrigenCalculo(),
                recomendacion.getProducto().getId(),
                recomendacion.getProducto().getSku(),
                recomendacion.getProducto().getNombre(),
                recomendacion.getProducto().getStockMinimo(),
                origen == null ? null : origen.getId(),
                origen == null ? null : origen.getCodigo(),
                destino == null ? null : destino.getId(),
                destino == null ? null : destino.getCodigo(),
                recomendacion.getCantidadSugerida(),
                recomendacion.getDiasCobertura(),
                recomendacion.getDescripcion(),
                recomendacion.getUsuarioDecision(),
                recomendacion.getMotivoDecision(),
                recomendacion.getFechaDecision(),
                recomendacion.getFechaCreacion());
    }
}
