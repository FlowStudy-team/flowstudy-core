package com.flowstudy.core.module.problem.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.module.problem.entity.CodeTemplate;
import com.flowstudy.core.module.problem.entity.Problem;
import com.flowstudy.core.module.problem.entity.ProblemSampleCase;
import com.flowstudy.core.module.problem.mapper.ProblemMapper;
import com.flowstudy.core.module.problem.vo.ProblemDetailResponse;
import com.flowstudy.core.module.problem.vo.ProblemSampleCaseResponse;
import com.flowstudy.core.module.problem.vo.ProblemSummaryResponse;
import com.flowstudy.core.module.problem.vo.ProblemTemplateResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProblemService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProblemMapper problemMapper;

    public ProblemService(ProblemMapper problemMapper) {
        this.problemMapper = problemMapper;
    }

    public PageResponse<ProblemSummaryResponse> getPublishedProblems(
            Long chapterId,
            String difficulty,
            String keyword,
            Integer page,
            Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : Math.min(size, MAX_PAGE_SIZE);
        String normalizedDifficulty = normalizeDifficulty(difficulty);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        long total = problemMapper.countPublished(chapterId, normalizedDifficulty, normalizedKeyword);
        List<ProblemSummaryResponse> records = problemMapper.findPublishedPage(
                        chapterId,
                        normalizedDifficulty,
                        normalizedKeyword,
                        safeSize,
                        (safePage - 1) * safeSize)
                .stream()
                .map(ProblemSummaryResponse::from)
                .toList();
        return new PageResponse<>(records, total, safePage, safeSize);
    }

    public ProblemDetailResponse getPublishedProblem(Long problemId) {
        Problem problem = findPublishedProblemOrThrow(problemId);
        List<ProblemSampleCaseResponse> sampleCases = problemMapper.findSampleCases(problemId).stream()
                .map(ProblemSampleCaseResponse::from)
                .toList();
        return ProblemDetailResponse.from(problem, sampleCases);
    }

    public Problem ensurePublishedProblemExists(Long problemId) {
        return findPublishedProblemOrThrow(problemId);
    }

    public List<ProblemSampleCase> getJudgeCases(Long problemId) {
        findPublishedProblemOrThrow(problemId);
        return problemMapper.findJudgeCases(problemId);
    }

    public ProblemTemplateResponse getCodeTemplate(Long problemId, String language) {
        findPublishedProblemOrThrow(problemId);
        String normalizedLanguage = normalizeLanguage(language);
        CodeTemplate template = problemMapper.findCodeTemplate(problemId, normalizedLanguage);
        if (template == null) {
            throw new BusinessException(42006, "code template does not exist", HttpStatus.NOT_FOUND);
        }
        return ProblemTemplateResponse.from(template);
    }

    public CodeTemplate findCodeTemplateForJudge(Long problemId, String language) {
        return problemMapper.findCodeTemplate(problemId, normalizeLanguage(language));
    }

    public void incrementSubmitCount(Long problemId) {
        problemMapper.incrementSubmitCount(problemId);
    }

    private Problem findPublishedProblemOrThrow(Long problemId) {
        Problem problem = problemMapper.findPublishedById(problemId);
        if (problem == null) {
            throw new BusinessException(42002, "problem does not exist", HttpStatus.NOT_FOUND);
        }
        return problem;
    }

    private String normalizeDifficulty(String difficulty) {
        return difficulty == null || difficulty.isBlank()
                ? null
                : difficulty.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            throw new BusinessException(40000, "language is required", HttpStatus.BAD_REQUEST);
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }
}
