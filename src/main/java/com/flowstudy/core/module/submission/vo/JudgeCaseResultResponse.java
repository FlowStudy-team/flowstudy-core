package com.flowstudy.core.module.submission.vo;

import com.flowstudy.core.module.submission.entity.JudgeCaseResult;

public record JudgeCaseResultResponse(
        Integer caseIndex,
        String status,
        Integer timeUsedMs,
        Integer memoryUsedKb,
        String expectedOutput,
        String actualOutput,
        String errorMessage) {

    public static JudgeCaseResultResponse from(JudgeCaseResult result) {
        return new JudgeCaseResultResponse(
                result.getCaseIndex(),
                result.getStatus(),
                result.getTimeUsedMs(),
                result.getMemoryUsedKb(),
                result.getExpectedOutput(),
                result.getActualOutput(),
                result.getErrorMessage());
    }
}
