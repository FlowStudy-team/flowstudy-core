package com.flowstudy.core.module.submission.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.common.trace.TraceContext;
import com.flowstudy.core.module.problem.entity.Problem;
import com.flowstudy.core.module.problem.service.ProblemService;
import com.flowstudy.core.module.submission.dto.CreateSubmissionRequest;
import com.flowstudy.core.module.submission.entity.Submission;
import com.flowstudy.core.module.submission.mapper.SubmissionMapper;
import com.flowstudy.core.module.submission.vo.CreateSubmissionResponse;
import com.flowstudy.core.module.submission.vo.SubmissionDetailResponse;
import com.flowstudy.core.module.submission.vo.SubmissionSummaryResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String PENDING_STATUS = "PENDING";

    private final SubmissionMapper submissionMapper;
    private final ProblemService problemService;

    public SubmissionService(SubmissionMapper submissionMapper, ProblemService problemService) {
        this.submissionMapper = submissionMapper;
        this.problemService = problemService;
    }

    @Transactional
    public CreateSubmissionResponse createPendingSubmission(
            Long userId, Long problemId, CreateSubmissionRequest request) {
        Problem problem = problemService.ensurePublishedProblemExists(problemId);
        String language = normalizeLanguage(request.language());
        ensureLanguageSupported(problem, language);

        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(problemId);
        submission.setLanguage(language);
        submission.setCode(request.code());
        submission.setStatus(PENDING_STATUS);
        submission.setScore(0);
        submission.setTraceId(TraceContext.getTraceId());
        submissionMapper.insert(submission);
        return new CreateSubmissionResponse(submission.getId(), submission.getStatus());
    }

    public SubmissionDetailResponse getSubmissionDetail(Long userId, Long submitId) {
        Submission submission = findOwnSubmissionOrThrow(userId, submitId);
        return SubmissionDetailResponse.from(submission, List.of());
    }

    public PageResponse<SubmissionSummaryResponse> getMySubmissions(
            Long userId, Long problemId, Integer page, Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : Math.min(size, MAX_PAGE_SIZE);
        long total = submissionMapper.countByUserId(userId, problemId);
        List<SubmissionSummaryResponse> records = submissionMapper
                .findPageByUserId(userId, problemId, safeSize, (safePage - 1) * safeSize)
                .stream()
                .map(SubmissionSummaryResponse::from)
                .toList();
        return new PageResponse<>(records, total, safePage, safeSize);
    }

    private Submission findOwnSubmissionOrThrow(Long userId, Long submitId) {
        Submission submission = submissionMapper.findByIdAndUserId(submitId, userId);
        if (submission == null) {
            throw new BusinessException(43000, "submission does not exist", HttpStatus.NOT_FOUND);
        }
        return submission;
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
