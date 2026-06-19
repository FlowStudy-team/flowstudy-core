package com.flowstudy.core.module.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubmissionRequest(
        @NotBlank(message = "language is required")
        @Size(max = 32, message = "language is too long")
        String language,
        @NotBlank(message = "code is required")
        @Size(max = 65535, message = "code is too long")
        String code) {
}
