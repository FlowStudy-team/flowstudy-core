package com.flowstudy.core.module.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBlogRequest(
        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title is too long")
        String title,

        @NotBlank(message = "contentMd is required")
        @Size(max = 65535, message = "contentMd is too long")
        String contentMd,

        @Size(max = 512, message = "summary is too long")
        String summary,

        Long tutorialId,

        Integer estimatedMinutes,

        @Size(max = 32, message = "status is too long")
        String status
) {}
