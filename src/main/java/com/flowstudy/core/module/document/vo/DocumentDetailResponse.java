package com.flowstudy.core.module.document.vo;

import com.flowstudy.core.module.document.entity.Document;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record DocumentDetailResponse(
        Long id,
        String title,
        String content,
        String summary,
        Long folderId,
        String folderName,
        Long categoryId,
        String categoryName,
        List<String> tags,
        String status,
        String createdAt,
        String updatedAt,
        String publishedAt) {

    public static DocumentDetailResponse from(Document doc) {
        List<String> tagList = doc.getTags() != null && !doc.getTags().isBlank()
                ? Arrays.asList(doc.getTags().split(","))
                : Collections.emptyList();
        return new DocumentDetailResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getContent(),
                doc.getSummary(),
                doc.getFolderId(),
                doc.getFolderName(),
                doc.getCategoryId(),
                doc.getCategoryName(),
                tagList,
                doc.getStatus(),
                DocumentItemResponse.from(doc).createdAt(),
                DocumentItemResponse.from(doc).updatedAt(),
                DocumentItemResponse.from(doc).publishedAt());
    }
}
