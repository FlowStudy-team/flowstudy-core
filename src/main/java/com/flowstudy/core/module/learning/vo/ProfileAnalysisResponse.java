package com.flowstudy.core.module.learning.vo;

public record ProfileAnalysisResponse(
        Object ability,
        Object weakPoints,
        Object codingStyle,
        String summaryMd,
        String learningSummaryMd) {
}
