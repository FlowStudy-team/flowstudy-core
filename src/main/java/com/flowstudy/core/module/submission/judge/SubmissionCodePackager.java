package com.flowstudy.core.module.submission.judge;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.module.problem.entity.CodeTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SubmissionCodePackager {

    public static final String FULL_PROGRAM = "FULL_PROGRAM";
    public static final String TEMPLATE_WRAPPED = "TEMPLATE_WRAPPED";
    private static final String USER_CODE_PLACEHOLDER = "{{USER_CODE}}";

    public PackagedSubmissionCode packageCode(String rawCode, CodeTemplate template) {
        if (template == null || template.getJudgeWrapperCode() == null
                || template.getJudgeWrapperCode().isBlank()) {
            return new PackagedSubmissionCode(rawCode, FULL_PROGRAM);
        }

        String wrapper = template.getJudgeWrapperCode();
        if (!wrapper.contains(USER_CODE_PLACEHOLDER)) {
            throw new BusinessException(
                    50002,
                    "judge wrapper placeholder {{USER_CODE}} is missing",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new PackagedSubmissionCode(
                wrapper.replace(USER_CODE_PLACEHOLDER, rawCode),
                TEMPLATE_WRAPPED);
    }
}
