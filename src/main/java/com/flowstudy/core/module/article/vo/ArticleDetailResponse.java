package com.flowstudy.core.module.article.vo;

import com.flowstudy.core.module.article.entity.Article;
import com.flowstudy.core.module.chapter.vo.ChapterSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;

public record ArticleDetailResponse(
        Long id,
        String title,
        String summary,
        String markdown,
        String coverUrl,
        AuthorResponse author,
        Integer chapterCount,
        Integer problemCount,
        Long viewCount,
        Long likeCount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ChapterSummaryResponse> chapters) {

    public static ArticleDetailResponse from(Article article, List<ChapterSummaryResponse> chapters) {
        return new ArticleDetailResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getSummary(),
                article.getCoverUrl(),
                new AuthorResponse(article.getAuthorId(), article.getAuthorName()),
                article.getChapterCount(),
                article.getProblemCount(),
                article.getViewCount(),
                article.getLikeCount(),
                article.getStatus(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                chapters);
    }
}
