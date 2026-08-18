package com.flowstudy.core.module.learning.vo;

import java.time.LocalDateTime;

public record LearningNoteResponse(
        Long id,
        String title,
        String contentMd,
        String source,
        String status,
        LocalDateTime createdAt) {
}
