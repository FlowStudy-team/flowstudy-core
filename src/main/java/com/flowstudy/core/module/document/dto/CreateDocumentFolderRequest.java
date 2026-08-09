package com.flowstudy.core.module.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDocumentFolderRequest(
        @NotBlank(message = "name is required")
        @Size(max = 128, message = "name is too long")
        String name,

        Long parentId
) {}
