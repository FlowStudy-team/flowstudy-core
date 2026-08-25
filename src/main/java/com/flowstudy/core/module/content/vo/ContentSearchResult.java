package com.flowstudy.core.module.content.vo;

public record ContentSearchResult(
        String type,
        Long id,
        String title,
        String summary,
        String url,
        double score) {
}
