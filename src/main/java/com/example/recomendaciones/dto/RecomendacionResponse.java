package com.example.recomendaciones.dto;

import com.example.pronostico.NivelRiesgo;
import com.example.pronostico.OrigenCalculo;
import com.example.recomendaciones.model.EstadoRecomendacion;
import com.example.recomendaciones.model.TipoRecomendacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecomendacionResponse(
        Long id,
        TipoRecomendacion tipo,
        EstadoRecomendacion estado,
        NivelRiesgo riesgo,
        OrigenCalculo origenCalculo,
        Long productoId,
        String sku,
        String productoNombre,
        Integer stockMinimo,
        Long bodegaOrigenId,
        String bodegaOrigenCodigo,
        Long bodegaDestinoId,
        String bodegaDestinoCodigo,
        Integer cantidadSugerida,
        BigDecimal diasCobertura,
        String descripcion,
        String usuarioDecision,
        String motivoDecision,
        LocalDateTime fechaDecision,
        LocalDateTime fechaCreacion) {
}
