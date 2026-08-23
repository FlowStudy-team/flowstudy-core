package com.flowstudy.core.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppendAiMessageRequest(
        @NotBlank String role,
        @NotBlank @Size(max = 100000) String content,
        String modelName,
        String traceId) {
}
