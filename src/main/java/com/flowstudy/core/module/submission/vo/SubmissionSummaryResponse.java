package com.flowstudy.core.module.submission.vo;

import com.flowstudy.core.module.submission.entity.Submission;
import java.time.LocalDateTime;

public record SubmissionSummaryResponse(
        Long submitId,
        Long problemId,
        String problemTitle,
        String language,
        String status,
        Integer timeUsedMs,
        Integer memoryUsedKb,
        Integer score,
        LocalDateTime createdAt) {

    public static SubmissionSummaryResponse from(Submission submission) {
        return new SubmissionSummaryResponse(
                submission.getId(),
                submission.getProblemId(),
                submission.getProblemTitle(),
                submission.getLanguage(),
                submission.getStatus(),
                submission.getTimeUsedMs(),
                submission.getMemoryUsedKb(),
                submission.getScore(),
                submission.getCreatedAt());
    }
}
