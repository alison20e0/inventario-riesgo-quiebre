package com.example.inventario.dto;

import com.example.pronostico.NivelRiesgo;
import com.example.pronostico.OrigenCalculo;

import java.math.BigDecimal;

public record ExistenciaResponse(
        Long id,
        Long bodegaId,
        String bodegaCodigo,
        String bodegaNombre,
        Long productoId,
        String sku,
        String productoNombre,
        Integer stockMinimo,
        Integer disponible,
        Integer reservado,
        Integer disponibleLibre,
        BigDecimal consumoDiario,
        BigDecimal diasCobertura,
        NivelRiesgo riesgo,
        OrigenCalculo origenCalculo,
        Long version) {
}
