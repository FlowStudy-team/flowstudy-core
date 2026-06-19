package com.flowstudy.core.module.submission.vo;

public record JudgeCaseResultResponse(
        Integer caseIndex,
        String status,
        Integer timeUsedMs,
        Integer memoryUsedKb,
        String input,
        String expectedOutput,
        String actualOutput,
        String errorMessage) {
}
