package com.example.messaging;

import com.example.recomendaciones.model.TipoRecomendacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SistemaComprasListener {

    private static final Logger log = LoggerFactory.getLogger(SistemaComprasListener.class);

    private final boolean simularFallo;

    public SistemaComprasListener(@Value("${app.compras.simular-fallo:false}") boolean simularFallo) {
        this.simularFallo = simularFallo;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_COMPRAS)
    public void procesar(RecomendacionAprobada evento) {
        if (evento.tipo() == TipoRecomendacion.COMPRA) {
            log.info("[SISTEMA-COMPRAS] Orden de compra {} enviada al proveedor por {} unidades de {} "
                            + "para la bodega {}. Riesgo {} calculado con {} y aprobada por {} ({}).",
                    evento.recomendacionId(), evento.cantidad(), evento.sku(), evento.bodegaDestinoCodigo(),
                    evento.riesgo(), evento.origenCalculo(), evento.aprobadoPor(), evento.motivo());
        } else {
            log.info("[SISTEMA-COMPRAS] Transferencia {} registrada: {} unidades de {} desde {} hacia {}. "
                            + "Riesgo {} calculado con {} y aprobada por {} ({}).",
                    evento.recomendacionId(), evento.cantidad(), evento.sku(), evento.bodegaOrigenCodigo(),
                    evento.bodegaDestinoCodigo(), evento.riesgo(), evento.origenCalculo(), evento.aprobadoPor(),
                    evento.motivo());
        }
        if (simularFallo) {
            throw new IllegalStateException("Falla simulada del sistema de compras (COMPRAS_SIMULAR_FALLO=true)");
        }
    }
}
