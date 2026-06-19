package com.flowstudy.core.module.problem.vo;

import com.flowstudy.core.module.problem.entity.Problem;

public record ProblemSummaryResponse(
        Long id,
        Long chapterId,
        String title,
        String difficulty,
        Long acceptedCount,
        Long submitCount) {

    public static ProblemSummaryResponse from(Problem problem) {
        return new ProblemSummaryResponse(
                problem.getId(),
                problem.getChapterId(),
                problem.getTitle(),
                problem.getDifficulty(),
                problem.getAcceptedCount(),
                problem.getSubmitCount());
    }
}
