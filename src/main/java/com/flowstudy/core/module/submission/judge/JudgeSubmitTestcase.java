package com.flowstudy.core.module.submission.judge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flowstudy.core.module.problem.entity.ProblemSampleCase;
import com.flowstudy.core.module.submission.dto.CreateCodeRunRequest;

public record JudgeSubmitTestcase(
        @JsonProperty("testcase_id") Long testcaseId,
        @JsonProperty("case_index") Integer caseIndex,
        String input,
        @JsonProperty("expected_output") String expectedOutput) {

    public static JudgeSubmitTestcase from(ProblemSampleCase testcase) {
        return new JudgeSubmitTestcase(
                testcase.getId(),
                testcase.getSortOrder(),
                testcase.getInputText(),
                testcase.getExpectedOutput());
    }

    public static JudgeSubmitTestcase fromRunCase(
            CreateCodeRunRequest.RunTestCaseRequest testcase,
            int caseIndex) {
        return new JudgeSubmitTestcase(
                null,
                caseIndex,
                testcase.input(),
                testcase.expectedOutput());
    }
}
