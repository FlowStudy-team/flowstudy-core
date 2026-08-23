package com.flowstudy.core.module.ai.vo;

import java.time.LocalDateTime;

public record AiMessageResponse(
        Long id,
        Long conversationId,
        String role,
        String content,
        String modelName,
        String traceId,
        LocalDateTime createdAt) {
}
