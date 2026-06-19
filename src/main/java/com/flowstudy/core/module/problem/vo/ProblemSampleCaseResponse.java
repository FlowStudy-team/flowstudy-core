package com.flowstudy.core.module.problem.vo;

import com.flowstudy.core.module.problem.entity.ProblemSampleCase;

public record ProblemSampleCaseResponse(String input, String output) {

    public static ProblemSampleCaseResponse from(ProblemSampleCase sampleCase) {
        return new ProblemSampleCaseResponse(sampleCase.getInputText(), sampleCase.getExpectedOutput());
    }
}
