package com.flowstudy.core.module.learning.controller;

import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.learning.dto.LearningEventRequest;
import com.flowstudy.core.module.learning.dto.CreateLearningNoteRequest;
import com.flowstudy.core.module.learning.service.LearningService;
import com.flowstudy.core.module.learning.vo.LearningNoteResponse;
import com.flowstudy.core.module.learning.vo.ProfileAnalysisResponse;
import com.flowstudy.core.module.learning.vo.UserProfileResponse;
import com.flowstudy.core.module.learning.vo.LearningOverviewResponse;
import java.time.LocalDate;
import com.flowstudy.core.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @PostMapping("/events")
    public Result<Void> recordEvent(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody LearningEventRequest request) {
        learningService.recordEvent(user.id(), request);
        return Result.success(null);
    }

    @PostMapping("/profile/analyze")
    public Result<ProfileAnalysisResponse> analyzeProfile(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return Result.success(learningService.analyze(user.id()));
    }

    @GetMapping("/profile")
    public Result<UserProfileResponse> getProfile(@AuthenticationPrincipal AuthenticatedUser user) {
        return Result.success(learningService.getProfile(user.id()));
    }

    @GetMapping("/notes")
    public Result<List<LearningNoteResponse>> getNotes(@AuthenticationPrincipal AuthenticatedUser user) {
        return Result.success(learningService.getRecentNotes(user.id()));
    }

    @PostMapping("/notes")
    public Result<LearningNoteResponse> createNote(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateLearningNoteRequest request) {
        return Result.success(learningService.createNote(user.id(), request));
    }

    @GetMapping("/overview")
    public Result<LearningOverviewResponse> getOverview(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return Result.success(learningService.getOverview(user.id(), startDate, endDate));
    }
}
