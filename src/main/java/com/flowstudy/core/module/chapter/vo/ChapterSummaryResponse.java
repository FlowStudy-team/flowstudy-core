package com.flowstudy.core.module.chapter.vo;

import com.flowstudy.core.module.chapter.entity.Chapter;
import java.util.List;

public record ChapterSummaryResponse(
        Long id,
        Long articleId,
        String title,
        Integer sortOrder,
        Integer estimatedMinutes,
        Integer problemCount,
        List<String> problemIds) {

    public static ChapterSummaryResponse from(Chapter chapter, List<Long> problemIds) {
        List<String> problemIdStrings = problemIds.stream().map(String::valueOf).toList();
        return new ChapterSummaryResponse(
                chapter.getId(),
                chapter.getArticleId(),
                chapter.getTitle(),
                chapter.getSortOrder(),
                chapter.getEstimatedMinutes(),
                chapter.getProblemCount(),
                problemIdStrings);
    }
}
