package com.flowstudy.core.module.submission.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.trace.TraceContext;
import com.flowstudy.core.module.problem.entity.CodeTemplate;
import com.flowstudy.core.module.problem.entity.Problem;
import com.flowstudy.core.module.problem.service.ProblemService;
import com.flowstudy.core.module.submission.dto.CreateCodeRunRequest;
import com.flowstudy.core.module.submission.entity.CodeRun;
import com.flowstudy.core.module.submission.judge.JudgeSubmitMessage;
import com.flowstudy.core.module.submission.judge.JudgeTaskPublisher;
import com.flowstudy.core.module.submission.judge.PackagedSubmissionCode;
import com.flowstudy.core.module.submission.judge.SubmissionCodePackager;
import com.flowstudy.core.module.submission.mapper.CodeRunCaseResultMapper;
import com.flowstudy.core.module.submission.mapper.CodeRunMapper;
import com.flowstudy.core.module.submission.vo.CodeRunDetailResponse;
import com.flowstudy.core.module.submission.vo.CreateCodeRunResponse;
import com.flowstudy.core.module.submission.vo.JudgeCaseResultResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeRunService {

    private static final String PENDING_STATUS = "PENDING";

    private final CodeRunMapper codeRunMapper;
    private final CodeRunCaseResultMapper codeRunCaseResultMapper;
    private final ProblemService problemService;
    private final JudgeTaskPublisher judgeTaskPublisher;
    private final SubmissionCodePackager submissionCodePackager;

    public CodeRunService(
            CodeRunMapper codeRunMapper,
            CodeRunCaseResultMapper codeRunCaseResultMapper,
            ProblemService problemService,
            JudgeTaskPublisher judgeTaskPublisher,
            SubmissionCodePackager submissionCodePackager) {
        this.codeRunMapper = codeRunMapper;
        this.codeRunCaseResultMapper = codeRunCaseResultMapper;
        this.problemService = problemService;
        this.judgeTaskPublisher = judgeTaskPublisher;
        this.submissionCodePackager = submissionCodePackager;
    }

    @Transactional
    public CreateCodeRunResponse createRun(Long userId, Long problemId, CreateCodeRunRequest request) {
        Problem problem = problemService.ensurePublishedProblemExists(problemId);
        String language = normalizeLanguage(request.language());
        ensureLanguageSupported(problem, language);

        CodeTemplate template = problemService.findCodeTemplateForJudge(problemId, language);
        PackagedSubmissionCode packagedCode = submissionCodePackager.packageCode(request.code(), template);

        CodeRun run = new CodeRun();
        run.setUserId(userId);
        run.setProblemId(problemId);
        run.setLanguage(language);
        run.setCode(request.code());
        run.setJudgeCode(packagedCode.judgeCode());
        run.setSubmitMode(packagedCode.submitMode());
        run.setStatus(PENDING_STATUS);
        run.setTraceId(TraceContext.getTraceId());
        codeRunMapper.insert(run);

        judgeTaskPublisher.publish(JudgeSubmitMessage.run(
                run.getId(),
                userId,
                problem,
                language,
                packagedCode.submitMode(),
                packagedCode.judgeCode(),
                request.testCases()));
        return new CreateCodeRunResponse(run.getId(), run.getStatus());
    }

    public CodeRunDetailResponse getRunDetail(Long userId, Long runId) {
        CodeRun run = codeRunMapper.findByIdAndUserId(runId, userId);
        if (run == null) {
            throw new BusinessException(43010, "code run does not exist", HttpStatus.NOT_FOUND);
        }
        List<JudgeCaseResultResponse> caseResults = codeRunCaseResultMapper.findByRunId(run.getId())
                .stream()
                .map(JudgeCaseResultResponse::from)
                .toList();
        return CodeRunDetailResponse.from(run, caseResults);
    }

    private String normalizeLanguage(String language) {
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureLanguageSupported(Problem problem, String language) {
        boolean supported = Arrays.stream(problem.getSupportLanguages().split(","))
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .anyMatch(language::equals);
        if (!supported) {
            throw new BusinessException(40000, "language is not supported by this problem", HttpStatus.BAD_REQUEST);
        }
    }
}
