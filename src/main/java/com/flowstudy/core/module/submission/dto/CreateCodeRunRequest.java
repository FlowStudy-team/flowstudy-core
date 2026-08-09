package com.flowstudy.core.module.submission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateCodeRunRequest(
        @NotBlank(message = "language is required")
        @Size(max = 32, message = "language is too long")
        String language,
        @NotBlank(message = "code is required")
        @Size(max = 65535, message = "code is too long")
        String code,
        @NotEmpty(message = "testCases is required")
        @Size(max = 10, message = "testCases size must be less than or equal to 10")
        List<@Valid RunTestCaseRequest> testCases) {

    public record RunTestCaseRequest(
            @NotNull(message = "input is required")
            @Size(max = 65535, message = "input is too long")
            String input,
            @NotNull(message = "expectedOutput is required")
            @Size(max = 65535, message = "expectedOutput is too long")
            String expectedOutput) {
    }
}
