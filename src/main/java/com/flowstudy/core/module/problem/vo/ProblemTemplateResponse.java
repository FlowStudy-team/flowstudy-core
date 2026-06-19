package com.flowstudy.core.module.problem.vo;

import com.flowstudy.core.module.problem.entity.CodeTemplate;

public record ProblemTemplateResponse(Long problemId, String language, String code) {

    public static ProblemTemplateResponse from(CodeTemplate template) {
        return new ProblemTemplateResponse(
                template.getProblemId(),
                template.getLanguage(),
                template.getTemplateCode());
    }
}
