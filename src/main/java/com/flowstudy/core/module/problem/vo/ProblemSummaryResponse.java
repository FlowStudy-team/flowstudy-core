package com.flowstudy.core.module.problem.vo;

import com.flowstudy.core.module.problem.entity.Problem;

public record ProblemSummaryResponse(
        Long id,
        Long blogId,
        String title,
        String difficulty,
        Long acceptedCount,
        Long submitCount) {

    public static ProblemSummaryResponse from(Problem problem) {
        return new ProblemSummaryResponse(
                problem.getId(),
                problem.getBlogId(),
                problem.getTitle(),
                problem.getDifficulty(),
                problem.getAcceptedCount(),
                problem.getSubmitCount());
    }
}
