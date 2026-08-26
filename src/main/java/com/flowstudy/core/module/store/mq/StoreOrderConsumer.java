package com.flowstudy.core.module.store.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowstudy.core.module.store.service.SeckillReservationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StoreOrderConsumer {
    private final ObjectMapper objectMapper;
    private final StoreOrderProcessor processor;
    private final SeckillReservationService reservations;

    public StoreOrderConsumer(
            ObjectMapper objectMapper,
            StoreOrderProcessor processor,
            SeckillReservationService reservations) {
        this.objectMapper = objectMapper;
        this.processor = processor;
        this.reservations = reservations;
    }

    @RabbitListener(queues = "${flowstudy.store.rabbitmq.queue}", containerFactory = "storeOrderListenerContainerFactory")
    public void consume(String payload) throws Exception {
        StoreOrderMessage message = objectMapper.readValue(payload, StoreOrderMessage.class);
        try {
            processor.process(message);
            // Mark the Redis reservation only after the DB transaction has committed.
            reservations.complete(message.orderNo());
        } catch (RuntimeException exception) {
            // The Lua release operation is idempotent, so retries cannot inflate stock.
            reservations.release(message.productId(), message.orderNo());
            throw exception;
        }
    }
}
