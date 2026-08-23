package com.flowstudy.core.module.ai.dto;

public record CreateAiConversationRequest(
        String title,
        Long tutorialId,
        Long blogId,
        Long problemId) {
}
