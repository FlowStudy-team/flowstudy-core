package com.flowstudy.core.module.learning.vo;

import java.time.LocalDate;
import java.util.List;

public record LearningOverviewResponse(
        long submissionCount,
        long acceptedSubmissionCount,
        long solvedProblemCount,
        long learningDays,
        int streakDays,
        List<DailyActivity> readingActivity,
        List<DailyActivity> submissionActivity) {

    public record DailyActivity(LocalDate date, int count) {}
}
