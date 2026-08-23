package com.flowstudy.core.module.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLearningNoteRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 200000) String contentMd) {
}
