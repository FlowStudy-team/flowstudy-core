package com.flowstudy.core.module.content.service;

import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.module.blog.mapper.BlogMapper;
import com.flowstudy.core.module.content.client.ContentSearchClient;
import com.flowstudy.core.module.content.vo.ContentSearchResponse;
import com.flowstudy.core.module.content.vo.ContentSearchResult;
import com.flowstudy.core.module.tutorial.mapper.TutorialMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContentSearchService {
    private final ContentSearchClient searchClient;
    private final BlogMapper blogMapper;
    private final TutorialMapper tutorialMapper;

    public ContentSearchService(ContentSearchClient searchClient, BlogMapper blogMapper, TutorialMapper tutorialMapper) {
        this.searchClient = searchClient;
        this.blogMapper = blogMapper;
        this.tutorialMapper = tutorialMapper;
    }

    public ContentSearchResponse search(String keyword, Integer size, String searchAfter) {
        String query = keyword == null ? "" : keyword.trim();
        int limit = size == null || size < 1 ? 10 : Math.min(size, 50);
        if (searchClient.isEnabled()) {
            try {
                return searchClient.search(query, limit, searchAfter);
            } catch (RuntimeException ignored) {
                // Keep the content center available while ES is starting or unavailable.
            }
        }
        PageResponse<ContentSearchResult> fallback = mysqlFallback(query, limit);
        return new ContentSearchResponse(fallback.records(), fallback.total(), null, "mysql");
    }

    private PageResponse<ContentSearchResult> mysqlFallback(String keyword, int limit) {
        List<ContentSearchResult> records = new ArrayList<>();
        blogMapper.findPublishedPage(null, false, keyword.isBlank() ? null : keyword, limit, 0)
                .forEach(blog -> records.add(new ContentSearchResult(
                        "BLOG", blog.getId(), blog.getTitle(), blog.getSummary(), "/blogs/" + blog.getId(),
                        blog.getLikeCount() == null ? 0 : blog.getLikeCount())));
        tutorialMapper.findPublishedPage(keyword.isBlank() ? null : keyword, limit, 0)
                .forEach(tutorial -> records.add(new ContentSearchResult(
                        "TUTORIAL", tutorial.getId(), tutorial.getTitle(), tutorial.getSummary(), "/tutorials/" + tutorial.getId(),
                        tutorial.getLikeCount() == null ? 0 : tutorial.getLikeCount())));
        records.sort(Comparator.comparingDouble(ContentSearchResult::score).reversed());
        long total = blogMapper.countPublished(null, false, keyword.isBlank() ? null : keyword)
                + tutorialMapper.countPublished(keyword.isBlank() ? null : keyword);
        return new PageResponse<>(records.stream().limit(limit).toList(), total, 1, limit);
    }
}
