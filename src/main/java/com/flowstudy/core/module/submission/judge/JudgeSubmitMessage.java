package com.flowstudy.core.module.submission.judge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flowstudy.core.module.problem.entity.Problem;
import com.flowstudy.core.module.problem.entity.ProblemSampleCase;
import com.flowstudy.core.module.submission.dto.CreateCodeRunRequest;
import java.util.List;
import java.util.stream.IntStream;

public record JudgeSubmitMessage(
        @JsonProperty("task_type") String taskType,
        @JsonProperty("run_id") Long runId,
        @JsonProperty("submission_id") Long submissionId,
        @JsonProperty("problem_id") Long problemId,
        @JsonProperty("user_id") Long userId,
        String language,
        @JsonProperty("submit_mode") String submitMode,
        String code,
        @JsonProperty("time_limit") Integer timeLimit,
        @JsonProperty("memory_limit") Integer memoryLimit,
        List<JudgeSubmitTestcase> testcases) {

    private static final String SUBMISSION_TASK = "SUBMISSION";
    private static final String RUN_TASK = "RUN";

    public static JudgeSubmitMessage from(
            Long submissionId,
            Long userId,
            Problem problem,
            String language,
            String submitMode,
            String code,
            List<ProblemSampleCase> testcases) {
        return new JudgeSubmitMessage(
                SUBMISSION_TASK,
                null,
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

    public static JudgeSubmitMessage run(
            Long runId,
            Long userId,
            Problem problem,
            String language,
            String submitMode,
            String code,
            List<CreateCodeRunRequest.RunTestCaseRequest> testcases) {
        return new JudgeSubmitMessage(
                RUN_TASK,
                runId,
                null,
                problem.getId(),
                userId,
                language,
                submitMode,
                code,
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb() * 1024,
                IntStream.range(0, testcases.size())
                        .mapToObj(index -> JudgeSubmitTestcase.fromRunCase(testcases.get(index), index + 1))
                        .toList());
    }
}
