package com.flowstudy.core.module.problem.vo;

import com.flowstudy.core.module.problem.entity.Problem;
import java.util.Arrays;
import java.util.List;

public record ProblemDetailResponse(
        Long id,
        Long blogId,
        String title,
        String descriptionMd,
        String difficulty,
        String inputDescription,
        String outputDescription,
        List<ProblemSampleCaseResponse> sampleCases,
        List<String> supportLanguages,
        Integer timeLimitMs,
        Integer memoryLimitMb,
        Long acceptedCount,
        Long submitCount) {

    public static ProblemDetailResponse from(Problem problem, List<ProblemSampleCaseResponse> sampleCases) {
        return new ProblemDetailResponse(
                problem.getId(),
                problem.getBlogId(),
                problem.getTitle(),
                problem.getDescriptionMd(),
                problem.getDifficulty(),
                problem.getInputDescription(),
                problem.getOutputDescription(),
                sampleCases,
                parseSupportLanguages(problem.getSupportLanguages()),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                problem.getAcceptedCount(),
                problem.getSubmitCount());
    }

    private static List<String> parseSupportLanguages(String supportLanguages) {
        if (supportLanguages == null || supportLanguages.isBlank()) {
            return List.of();
        }
        return Arrays.stream(supportLanguages.split(","))
                .map(String::trim)
                .filter(language -> !language.isBlank())
                .toList();
    }
}
