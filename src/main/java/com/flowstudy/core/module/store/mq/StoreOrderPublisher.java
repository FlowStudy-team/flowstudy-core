package com.flowstudy.core.module.store.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowstudy.core.common.exception.BusinessException;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class StoreOrderPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String routingKey;

    public StoreOrderPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${flowstudy.store.rabbitmq.exchange}") String exchange,
            @Value("${flowstudy.store.rabbitmq.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(StoreOrderMessage message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException exception) {
            throw new BusinessException(50003, "store order task serialization failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static StoreOrderMessage message(String traceId, String orderNo, Long userId, Long productId, Long couponId) {
        return new StoreOrderMessage(UUID.randomUUID().toString(), traceId, orderNo, userId, productId, couponId);
    }
}
