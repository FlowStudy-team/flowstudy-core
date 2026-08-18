package com.flowstudy.core.module.learning.dto;

import java.time.LocalDateTime;

public record LearningEventPayload(
        String eventType,
        String resourceType,
        Long resourceId,
        Integer durationSeconds,
        String extraJson,
        LocalDateTime occurredAt) {
}
