package com.flowstudy.core.module.submission.judge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flowstudy.core.module.problem.entity.Problem;
import com.flowstudy.core.module.problem.entity.ProblemSampleCase;
import java.util.List;

public record JudgeSubmitMessage(
        @JsonProperty("submission_id") Long submissionId,
        @JsonProperty("problem_id") Long problemId,
        @JsonProperty("user_id") Long userId,
        String language,
        @JsonProperty("submit_mode") String submitMode,
        String code,
        @JsonProperty("time_limit") Integer timeLimit,
        @JsonProperty("memory_limit") Integer memoryLimit,
        List<JudgeSubmitTestcase> testcases) {

    public static JudgeSubmitMessage from(
            Long submissionId,
            Long userId,
            Problem problem,
            String language,
            String submitMode,
            String code,
            List<ProblemSampleCase> testcases) {
        return new JudgeSubmitMessage(
                submissionId,
                problem.getId(),
                userId,
                language,
                submitMode,
                code,
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb() * 1024,
                testcases.stream()
                        .map(JudgeSubmitTestcase::from)
                        .toList());
    }
}
