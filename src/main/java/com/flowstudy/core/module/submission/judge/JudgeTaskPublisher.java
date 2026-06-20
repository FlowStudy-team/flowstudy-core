package com.flowstudy.core.module.submission.judge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowstudy.core.common.exception.BusinessException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JudgeTaskPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String queueName;

    public JudgeTaskPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${flowstudy.judge.rabbitmq.queue-name}") String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    public void publish(JudgeSubmitMessage message) {
        try {
            rabbitTemplate.convertAndSend(queueName, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException exception) {
            throw new BusinessException(50001, "judge task serialization failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
