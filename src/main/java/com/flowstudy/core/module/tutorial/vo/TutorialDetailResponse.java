package com.flowstudy.core.module.tutorial.vo;

import com.flowstudy.core.module.tutorial.entity.Tutorial;
import com.flowstudy.core.module.blog.vo.BlogSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;

public record TutorialDetailResponse(
        Long id,
        String title,
        String summary,
        String markdown,
        String coverUrl,
        AuthorResponse author,
        Integer blogCount,
        Integer problemCount,
        Long viewCount,
        Long likeCount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<BlogSummaryResponse> blogs) {

    public static TutorialDetailResponse from(Tutorial tutorial, List<BlogSummaryResponse> blogs) {
        return new TutorialDetailResponse(
                tutorial.getId(),
                tutorial.getTitle(),
                tutorial.getSummary(),
                tutorial.getSummary(),
                tutorial.getCoverUrl(),
                new AuthorResponse(tutorial.getAuthorId(), tutorial.getAuthorName()),
                tutorial.getBlogCount(),
                tutorial.getProblemCount(),
                tutorial.getViewCount(),
                tutorial.getLikeCount(),
                tutorial.getStatus(),
                tutorial.getCreatedAt(),
                tutorial.getUpdatedAt(),
                blogs);
    }
}
