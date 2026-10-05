package com.example.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PublicadorRecomendacionAprobada {

    private static final Logger log = LoggerFactory.getLogger(PublicadorRecomendacionAprobada.class);

    private final RabbitTemplate rabbitTemplate;

    public PublicadorRecomendacionAprobada(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicar(RecomendacionAprobada evento) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_EVENTOS, RabbitConfig.ROUTING_KEY_APROBADA, evento,
                    mensaje -> {
                        mensaje.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        mensaje.getMessageProperties().setHeader(RabbitConfig.HEADER_TYPE_ID,
                                RecomendacionAprobada.class.getName());
                        return mensaje;
                    });
            log.info("Evento RecomendacionAprobada publicado en {} con routing key {} (recomendacion {}).",
                    RabbitConfig.EXCHANGE_EVENTOS, RabbitConfig.ROUTING_KEY_APROBADA, evento.recomendacionId());
        } catch (AmqpException ex) {
            log.error("No se pudo publicar el evento RecomendacionAprobada de la recomendacion {}: {}",
                    evento.recomendacionId(), ex.getMessage());
        }
    }
}
