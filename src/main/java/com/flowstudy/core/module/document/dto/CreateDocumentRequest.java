package com.flowstudy.core.module.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateDocumentRequest(
        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title is too long")
        String title,

        @Size(max = 500000, message = "content is too long")
        String content,

        Long folderId,

        Long categoryId,

        List<String> tags
) {}
