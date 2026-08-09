package com.flowstudy.core.module.blog.vo;

import com.flowstudy.core.module.blog.entity.Blog;
import java.util.List;

public record BlogDetailResponse(
        Long id,
        Long tutorialId,
        String title,
        String contentMd,
        String markdown,
        Integer sortOrder,
        Integer estimatedMinutes,
        List<ProblemSummaryResponse> problems,
        List<String> problemIds,
        Long prevBlogId,
        Long nextBlogId,
        Long authorId,
        String authorName,
        String createdAt,
        String updatedAt,
        String publishedAt) {

    public static BlogDetailResponse from(
            Blog blog,
            List<ProblemSummaryResponse> problems,
            Long prevBlogId,
            Long nextBlogId) {
        return new BlogDetailResponse(
                blog.getId(),
                blog.getTutorialId(),
                blog.getTitle(),
                blog.getContentMd(),
                blog.getContentMd(),
                blog.getSortOrder(),
                blog.getEstimatedMinutes(),
                problems,
                problems.stream().map(problem -> String.valueOf(problem.id())).toList(),
                prevBlogId,
                nextBlogId,
                blog.getAuthorId(),
                blog.getAuthorName(),
                blog.getCreatedAt() != null ? blog.getCreatedAt().toString() : null,
                blog.getUpdatedAt() != null ? blog.getUpdatedAt().toString() : null,
                blog.getPublishedAt() != null ? blog.getPublishedAt().toString() : null);
    }
}
