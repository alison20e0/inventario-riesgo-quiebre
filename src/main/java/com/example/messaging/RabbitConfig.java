package com.example.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_EVENTOS = "inventario.eventos";
    public static final String EXCHANGE_ERRORES = "inventario.dlx";
    public static final String ROUTING_KEY_APROBADA = "recomendacion.aprobada";
    public static final String ROUTING_KEY_ERROR = "compras.errores";
    public static final String QUEUE_COMPRAS = "compras.solicitudes";
    public static final String QUEUE_ERRORES = "compras.solicitudes.errores";
    public static final String HEADER_TYPE_ID = "__TypeId__";
    public static final int MAX_INTENTOS = 3;

    @Bean
    public TopicExchange exchangeEventos() {
        return new TopicExchange(EXCHANGE_EVENTOS, true, false);
    }

    @Bean
    public TopicExchange exchangeErrores() {
        return new TopicExchange(EXCHANGE_ERRORES, true, false);
    }

    @Bean
    public Queue queueCompras() {
        return QueueBuilder.durable(QUEUE_COMPRAS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_ERRORES)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_ERROR)
                .build();
    }

    @Bean
    public Queue queueErrores() {
        return QueueBuilder.durable(QUEUE_ERRORES).build();
    }

    @Bean
    public Binding bindingQueueCompras(Queue queueCompras, TopicExchange exchangeEventos) {
        return BindingBuilder.bind(queueCompras).to(exchangeEventos).with(ROUTING_KEY_APROBADA);
    }

    @Bean
    public Binding bindingQueueErrores(Queue queueErrores, TopicExchange exchangeErrores) {
        return BindingBuilder.bind(queueErrores).to(exchangeErrores).with(ROUTING_KEY_ERROR);
    }

    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            RabbitTemplate rabbitTemplate,
            MessageConverter rabbitMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(MAX_INTENTOS)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate, EXCHANGE_ERRORES, ROUTING_KEY_ERROR))
                .build());
        return factory;
    }
}
