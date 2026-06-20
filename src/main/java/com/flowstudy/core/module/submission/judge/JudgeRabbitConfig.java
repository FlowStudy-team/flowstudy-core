package com.flowstudy.core.module.submission.judge;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JudgeRabbitConfig {

    @Bean
    Queue judgeSubmissionQueue(@Value("${flowstudy.judge.rabbitmq.queue-name}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }
}
