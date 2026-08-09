package com.flowstudy.core.module.document.vo;

import com.flowstudy.core.module.document.entity.Document;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record DocumentItemResponse(
        Long id,
        String title,
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

    public static DocumentItemResponse from(Document doc) {
        List<String> tagList = doc.getTags() != null && !doc.getTags().isBlank()
                ? Arrays.asList(doc.getTags().split(","))
                : Collections.emptyList();
        return new DocumentItemResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getSummary(),
                doc.getFolderId(),
                doc.getFolderName(),
                doc.getCategoryId(),
                doc.getCategoryName(),
                tagList,
                doc.getStatus(),
                format(doc.getCreatedAt()),
                format(doc.getUpdatedAt()),
                format(doc.getPublishedAt()));
    }

    private static String format(LocalDateTime dt) {
        return dt != null ? dt.toString() : null;
    }
}
