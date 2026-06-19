package com.flowstudy.core.module.submission.controller;

import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.submission.dto.CreateSubmissionRequest;
import com.flowstudy.core.module.submission.service.SubmissionService;
import com.flowstudy.core.module.submission.vo.CreateSubmissionResponse;
import com.flowstudy.core.module.submission.vo.SubmissionDetailResponse;
import com.flowstudy.core.module.submission.vo.SubmissionSummaryResponse;
import com.flowstudy.core.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/problems/{problemId}/submissions")
    public Result<CreateSubmissionResponse> createSubmission(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long problemId,
            @Valid @RequestBody CreateSubmissionRequest request) {
        return Result.success(submissionService.createPendingSubmission(user.id(), problemId, request));
    }

    @GetMapping("/submissions/{submitId}")
    public Result<SubmissionDetailResponse> getSubmissionDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long submitId) {
        return Result.success(submissionService.getSubmissionDetail(user.id(), submitId));
    }

    @GetMapping("/submissions/my")
    public Result<PageResponse<SubmissionSummaryResponse>> getMySubmissions(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Long problemId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(submissionService.getMySubmissions(user.id(), problemId, page, size));
    }
}
