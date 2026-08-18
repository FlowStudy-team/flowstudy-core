package com.flowstudy.core.module.learning.vo;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long userId,
        String abilityJson,
        String weakPointsJson,
        String codingStyleJson,
        String summaryMd,
        LocalDateTime updatedAt) {
}
