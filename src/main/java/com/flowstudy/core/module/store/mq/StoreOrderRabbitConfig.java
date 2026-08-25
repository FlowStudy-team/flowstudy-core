package com.flowstudy.core.module.store.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreOrderRabbitConfig {
    @Bean
    DirectExchange storeOrderExchange(@Value("${flowstudy.store.rabbitmq.exchange}") String name) {
        return new DirectExchange(name);
    }

    @Bean
    DirectExchange storeOrderDeadLetterExchange(@Value("${flowstudy.store.rabbitmq.dead-letter-exchange}") String name) {
        return new DirectExchange(name);
    }

    @Bean
    Queue storeOrderDeadLetterQueue(@Value("${flowstudy.store.rabbitmq.dead-letter-queue}") String name) {
        return new Queue(name, true);
    }

    @Bean
    Binding storeOrderDeadLetterBinding(
            @Qualifier("storeOrderDeadLetterQueue") Queue queue,
            @Qualifier("storeOrderDeadLetterExchange") DirectExchange exchange,
            @Value("${flowstudy.store.rabbitmq.dead-letter-routing-key}") String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    Queue storeOrderQueue(
            @Value("${flowstudy.store.rabbitmq.queue}") String name,
            @Value("${flowstudy.store.rabbitmq.dead-letter-exchange}") String deadLetterExchange,
            @Value("${flowstudy.store.rabbitmq.dead-letter-routing-key}") String deadLetterRoutingKey) {
        return new Queue(name, true, false, false, java.util.Map.of(
                "x-dead-letter-exchange", deadLetterExchange,
                "x-dead-letter-routing-key", deadLetterRoutingKey));
    }

    @Bean
    Binding storeOrderBinding(
            @Qualifier("storeOrderQueue") Queue queue,
            @Qualifier("storeOrderExchange") DirectExchange exchange,
            @Value("${flowstudy.store.rabbitmq.routing-key}") String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    SimpleRabbitListenerContainerFactory storeOrderListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Value("${flowstudy.store.rabbitmq.consumer-concurrency:2}") int concurrency,
            @Value("${flowstudy.store.rabbitmq.max-retries:3}") int maxRetries) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(Math.max(1, concurrency));
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(new org.springframework.retry.interceptor.RetryInterceptorBuilder.StatelessRetryInterceptorBuilder()
                .maxAttempts(Math.max(1, maxRetries))
                .backOffOptions(1000L, 2.0, 10000L)
                .recoverer(new org.springframework.retry.interceptor.RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
