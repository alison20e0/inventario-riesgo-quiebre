package com.example.messaging;

import com.example.pronostico.NivelRiesgo;
import com.example.pronostico.OrigenCalculo;
import com.example.recomendaciones.model.TipoRecomendacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecomendacionAprobada(
        Long recomendacionId,
        TipoRecomendacion tipo,
        NivelRiesgo riesgo,
        OrigenCalculo origenCalculo,
        String sku,
        String productoNombre,
        Long bodegaOrigenId,
        String bodegaOrigenCodigo,
        Long bodegaDestinoId,
        String bodegaDestinoCodigo,
        Integer cantidad,
        BigDecimal diasCobertura,
        String aprobadoPor,
        String motivo,
        LocalDateTime fechaAprobacion) {
}
