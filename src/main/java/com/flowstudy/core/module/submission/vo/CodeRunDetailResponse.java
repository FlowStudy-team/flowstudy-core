package com.flowstudy.core.module.submission.vo;

import com.flowstudy.core.module.submission.entity.CodeRun;
import java.time.LocalDateTime;
import java.util.List;

public record CodeRunDetailResponse(
        Long runId,
        Long problemId,
        String problemTitle,
        String language,
        String status,
        Integer timeUsedMs,
        Integer memoryUsedKb,
        String compileMessage,
        String runtimeMessage,
        LocalDateTime createdAt,
        List<JudgeCaseResultResponse> caseResults) {

    public static CodeRunDetailResponse from(CodeRun run, List<JudgeCaseResultResponse> caseResults) {
        return new CodeRunDetailResponse(
                run.getId(),
                run.getProblemId(),
                run.getProblemTitle(),
                run.getLanguage(),
                run.getStatus(),
                run.getTimeUsedMs(),
                run.getMemoryUsedKb(),
                run.getCompileMessage(),
                run.getRuntimeMessage(),
                run.getCreatedAt(),
                caseResults);
    }
}
