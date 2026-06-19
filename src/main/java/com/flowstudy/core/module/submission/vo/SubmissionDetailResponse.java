package com.flowstudy.core.module.submission.vo;

import com.flowstudy.core.module.submission.entity.Submission;
import java.time.LocalDateTime;
import java.util.List;

public record SubmissionDetailResponse(
        Long submitId,
        Long problemId,
        String problemTitle,
        String language,
        String status,
        Integer timeUsedMs,
        Integer memoryUsedKb,
        Integer score,
        String compileMessage,
        String runtimeMessage,
        LocalDateTime createdAt,
        List<JudgeCaseResultResponse> caseResults) {

    public static SubmissionDetailResponse from(Submission submission, List<JudgeCaseResultResponse> caseResults) {
        return new SubmissionDetailResponse(
                submission.getId(),
                submission.getProblemId(),
                submission.getProblemTitle(),
                submission.getLanguage(),
                submission.getStatus(),
                submission.getTimeUsedMs(),
                submission.getMemoryUsedKb(),
                submission.getScore(),
                submission.getCompileMessage(),
                submission.getRuntimeMessage(),
                submission.getCreatedAt(),
                caseResults);
    }
}
