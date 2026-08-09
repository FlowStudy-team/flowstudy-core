package com.flowstudy.core.module.blog.vo;

import com.flowstudy.core.module.blog.entity.Blog;
import java.util.List;

public record BlogSummaryResponse(
        Long id,
        Long tutorialId,
        String title,
        String summary,
        Integer sortOrder,
        Integer estimatedMinutes,
        Integer problemCount,
        List<String> problemIds,
        Long authorId,
        String authorName,
        Long viewCount,
        Long likeCount,
        String createdAt,
        String updatedAt,
        String publishedAt) {

    public static BlogSummaryResponse from(Blog blog, List<Long> problemIds) {
        List<String> problemIdStrings = problemIds.stream().map(String::valueOf).toList();
        return new BlogSummaryResponse(
                blog.getId(),
                blog.getTutorialId(),
                blog.getTitle(),
                blog.getSummary(),
                blog.getSortOrder(),
                blog.getEstimatedMinutes(),
                blog.getProblemCount(),
                problemIdStrings,
                blog.getAuthorId(),
                blog.getAuthorName(),
                blog.getViewCount(),
                blog.getLikeCount(),
                blog.getCreatedAt() != null ? blog.getCreatedAt().toString() : null,
                blog.getUpdatedAt() != null ? blog.getUpdatedAt().toString() : null,
                blog.getPublishedAt() != null ? blog.getPublishedAt().toString() : null);
    }
}
