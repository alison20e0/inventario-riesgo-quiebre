package com.example.pronostico;

import com.example.inventario.model.Existencia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PronosticoService {

    private static final Logger log = LoggerFactory.getLogger(PronosticoService.class);

    private static final BigDecimal DIAS_COBERTURA_RIESGO_ALTO = new BigDecimal("2");
    private static final BigDecimal DIAS_COBERTURA_RIESGO_MEDIO = new BigDecimal("5");
    private static final int ESCALA = 2;

    private final boolean simularFallo;

    public PronosticoService(@Value("${app.pronostico.simular-fallo:false}") boolean simularFallo) {
        this.simularFallo = simularFallo;
    }

    public PronosticoResultado calcular(Existencia existencia) {
        if (simularFallo) {
            throw new PronosticoNoDisponibleException(
                    "Falla simulada del motor de pronostico (PRONOSTICO_SIMULAR_FALLO=true)");
        }
        BigDecimal consumoDiario = existencia.getConsumoDiario();
        if (consumoDiario == null || consumoDiario.signum() <= 0) {
            throw new PronosticoNoDisponibleException(
                    "No existe consumo diario registrado para el producto " + existencia.getProducto().getSku());
        }
        BigDecimal diasCobertura = BigDecimal.valueOf(existencia.getDisponible())
                .divide(consumoDiario, ESCALA, RoundingMode.HALF_UP);
        return new PronosticoResultado(consumoDiario, diasCobertura, riesgoPorDiasCobertura(diasCobertura),
                OrigenCalculo.PRONOSTICO);
    }

    public PronosticoResultado evaluar(Existencia existencia) {
        try {
            return calcular(existencia);
        } catch (PronosticoNoDisponibleException ex) {
            log.warn("Pronostico no disponible para {} en {} ({}). Se usa el stock minimo como fallback.",
                    existencia.getProducto().getSku(), existencia.getBodega().getCodigo(), ex.getMessage());
            return new PronosticoResultado(null, null, riesgoPorStockMinimo(existencia), OrigenCalculo.FALLBACK_MINIMO);
        }
    }

    private NivelRiesgo riesgoPorDiasCobertura(BigDecimal diasCobertura) {
        if (diasCobertura.compareTo(DIAS_COBERTURA_RIESGO_ALTO) <= 0) {
            return NivelRiesgo.ALTO;
        }
        if (diasCobertura.compareTo(DIAS_COBERTURA_RIESGO_MEDIO) <= 0) {
            return NivelRiesgo.MEDIO;
        }
        return NivelRiesgo.BAJO;
    }

    private NivelRiesgo riesgoPorStockMinimo(Existencia existencia) {
        int stockMinimo = existencia.getProducto().getStockMinimo();
        int disponible = existencia.getDisponible();
        if (disponible <= stockMinimo) {
            return NivelRiesgo.ALTO;
        }
        if (disponible * 2 <= stockMinimo * 3) {
            return NivelRiesgo.MEDIO;
        }
        return NivelRiesgo.BAJO;
    }
}
