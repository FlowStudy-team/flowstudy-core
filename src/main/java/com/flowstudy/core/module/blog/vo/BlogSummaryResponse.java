package com.flowstudy.core.module.blog.vo;

import com.flowstudy.core.module.blog.entity.Blog;
import java.util.List;

public record BlogSummaryResponse(
        Long id,
        Long tutorialId,
        String title,
        Integer sortOrder,
        Integer estimatedMinutes,
        Integer problemCount,
        List<String> problemIds) {

    public static BlogSummaryResponse from(Blog blog, List<Long> problemIds) {
        List<String> problemIdStrings = problemIds.stream().map(String::valueOf).toList();
        return new BlogSummaryResponse(
                blog.getId(),
                blog.getTutorialId(),
                blog.getTitle(),
                blog.getSortOrder(),
                blog.getEstimatedMinutes(),
                blog.getProblemCount(),
                problemIdStrings);
    }
}
