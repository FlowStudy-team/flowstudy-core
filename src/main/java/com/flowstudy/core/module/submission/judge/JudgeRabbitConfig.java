package com.flowstudy.core.module.submission.judge;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JudgeRabbitConfig {

    @Bean
    DirectExchange judgeDeadLetterExchange(
            @Value("${flowstudy.judge.rabbitmq.dead-letter-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Queue judgeDeadLetterQueue(
            @Value("${flowstudy.judge.rabbitmq.dead-letter-queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    Binding judgeDeadLetterBinding(
            @Qualifier("judgeDeadLetterQueue") Queue judgeDeadLetterQueue,
            DirectExchange judgeDeadLetterExchange,
            @Value("${flowstudy.judge.rabbitmq.dead-letter-routing-key}") String routingKey) {
        return BindingBuilder.bind(judgeDeadLetterQueue)
                .to(judgeDeadLetterExchange)
                .with(routingKey);
    }

    @Bean
    Queue judgeSubmissionQueue(
            @Value("${flowstudy.judge.rabbitmq.queue-name}") String queueName,
            @Value("${flowstudy.judge.rabbitmq.dead-letter-exchange}") String deadLetterExchange,
            @Value("${flowstudy.judge.rabbitmq.dead-letter-routing-key}") String deadLetterRoutingKey) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }
}
