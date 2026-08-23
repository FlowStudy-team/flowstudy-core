package com.flowstudy.core.module.ai.vo;

import java.time.LocalDateTime;

public record AiConversationResponse(
        Long id,
        String title,
        String status,
        Long tutorialId,
        Long blogId,
        Long problemId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
