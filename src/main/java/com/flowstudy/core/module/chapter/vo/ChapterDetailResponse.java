package com.flowstudy.core.module.chapter.vo;

import com.flowstudy.core.module.chapter.entity.Chapter;
import java.util.List;

public record ChapterDetailResponse(
        Long id,
        Long articleId,
        String title,
        String contentMd,
        String markdown,
        Integer sortOrder,
        Integer estimatedMinutes,
        List<ProblemSummaryResponse> problems,
        List<String> problemIds,
        Long prevChapterId,
        Long nextChapterId) {

    public static ChapterDetailResponse from(
            Chapter chapter,
            List<ProblemSummaryResponse> problems,
            Long prevChapterId,
            Long nextChapterId) {
        return new ChapterDetailResponse(
                chapter.getId(),
                chapter.getArticleId(),
                chapter.getTitle(),
                chapter.getContentMd(),
                chapter.getContentMd(),
                chapter.getSortOrder(),
                chapter.getEstimatedMinutes(),
                problems,
                problems.stream().map(problem -> String.valueOf(problem.id())).toList(),
                prevChapterId,
                nextChapterId);
    }
}
