package com.flowstudy.core.module.content.vo;

import java.util.List;

public record ContentSearchResponse(
        List<ContentSearchResult> records,
        long total,
        String nextSearchAfter,
        String backend) {
}
