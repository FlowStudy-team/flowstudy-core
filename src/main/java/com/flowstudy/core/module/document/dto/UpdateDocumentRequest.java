package com.flowstudy.core.module.document.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateDocumentRequest(
        @Size(max = 255, message = "title is too long")
        String title,

        @Size(max = 500000, message = "content is too long")
        String content,

        @Size(max = 512, message = "summary is too long")
        String summary,

        Long folderId,

        Long categoryId,

        List<String> tags,

        @Size(max = 32, message = "status is too long")
        String status
) {}
