package com.example.pronostico;

import java.math.BigDecimal;

public record PronosticoResultado(
        BigDecimal consumoDiario,
        BigDecimal diasCobertura,
        NivelRiesgo riesgo,
        OrigenCalculo origenCalculo) {

    public boolean esFallback() {
        return origenCalculo == OrigenCalculo.FALLBACK_MINIMO;
    }
}
