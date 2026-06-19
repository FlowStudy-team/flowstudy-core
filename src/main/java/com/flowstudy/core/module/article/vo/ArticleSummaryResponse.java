package com.flowstudy.core.module.article.vo;

import com.flowstudy.core.module.article.entity.Article;
import java.time.LocalDateTime;
import java.util.List;

public record ArticleSummaryResponse(
        Long id,
        String title,
        String summary,
        String coverUrl,
        String authorName,
        Integer chapterCount,
        Integer problemCount,
        Long viewCount,
        Long likeCount,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> tags,
        String difficulty) {

    public static ArticleSummaryResponse from(Article article) {
        return new ArticleSummaryResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getCoverUrl(),
                article.getAuthorName(),
                article.getChapterCount(),
                article.getProblemCount(),
                article.getViewCount(),
                article.getLikeCount(),
                article.getSortOrder(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                List.of(),
                "Beginner");
    }
}
