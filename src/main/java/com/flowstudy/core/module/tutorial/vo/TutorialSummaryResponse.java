package com.flowstudy.core.module.tutorial.vo;

import com.flowstudy.core.module.tutorial.entity.Tutorial;
import java.time.LocalDateTime;
import java.util.List;

public record TutorialSummaryResponse(
        Long id,
        String title,
        String summary,
        String coverUrl,
        String authorName,
        Integer blogCount,
        Integer problemCount,
        Long viewCount,
        Long likeCount,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> tags,
        String difficulty) {

    public static TutorialSummaryResponse from(Tutorial tutorial) {
        return new TutorialSummaryResponse(
                tutorial.getId(),
                tutorial.getTitle(),
                tutorial.getSummary(),
                tutorial.getCoverUrl(),
                tutorial.getAuthorName(),
                tutorial.getBlogCount(),
                tutorial.getProblemCount(),
                tutorial.getViewCount(),
                tutorial.getLikeCount(),
                tutorial.getSortOrder(),
                tutorial.getCreatedAt(),
                tutorial.getUpdatedAt(),
                List.of(),
                "Beginner");
    }
}
