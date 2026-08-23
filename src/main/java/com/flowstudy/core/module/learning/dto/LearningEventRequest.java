package com.flowstudy.core.module.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record LearningEventRequest(
        @NotBlank @Size(max = 64) String eventType,
        @Size(max = 64) String resourceType,
        Long resourceId,
        Integer durationSeconds,
        @Size(max = 8000) String extraJson,
        LocalDateTime occurredAt) {
}
