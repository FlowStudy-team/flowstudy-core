package com.flowstudy.core.module.problem.controller;

import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.problem.service.ProblemService;
import com.flowstudy.core.module.problem.vo.ProblemDetailResponse;
import com.flowstudy.core.module.problem.vo.ProblemSummaryResponse;
import com.flowstudy.core.module.problem.vo.ProblemTemplateResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public Result<PageResponse<ProblemSummaryResponse>> listProblems(
            @RequestParam(required = false) Long blogId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(problemService.getPublishedProblems(
                blogId,
                difficulty,
                keyword,
                page,
                size == null ? pageSize : size));
    }

    @GetMapping("/{problemId}")
    public Result<ProblemDetailResponse> getProblem(@PathVariable Long problemId) {
        return Result.success(problemService.getPublishedProblem(problemId));
    }

    @GetMapping("/{problemId}/template")
    public Result<ProblemTemplateResponse> getTemplate(
            @PathVariable Long problemId,
            @RequestParam String language) {
        return Result.success(problemService.getCodeTemplate(problemId, language));
    }
}
