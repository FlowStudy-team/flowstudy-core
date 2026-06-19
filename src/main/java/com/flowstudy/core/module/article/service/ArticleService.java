package com.flowstudy.core.module.article.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.module.article.entity.Article;
import com.flowstudy.core.module.article.mapper.ArticleMapper;
import com.flowstudy.core.module.article.vo.ArticleDetailResponse;
import com.flowstudy.core.module.article.vo.ArticleSummaryResponse;
import com.flowstudy.core.module.chapter.service.ChapterService;
import com.flowstudy.core.module.chapter.vo.ChapterSummaryResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ArticleService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ArticleMapper articleMapper;
    private final ChapterService chapterService;

    public ArticleService(ArticleMapper articleMapper, ChapterService chapterService) {
        this.articleMapper = articleMapper;
        this.chapterService = chapterService;
    }

    public PageResponse<ArticleSummaryResponse> getPublishedArticles(Integer page, Integer size, String keyword) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : Math.min(size, MAX_PAGE_SIZE);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        long total = articleMapper.countPublished(normalizedKeyword);
        List<ArticleSummaryResponse> records = articleMapper.findPublishedPage(
                        normalizedKeyword,
                        safeSize,
                        (safePage - 1) * safeSize)
                .stream()
                .map(ArticleSummaryResponse::from)
                .toList();
        return new PageResponse<>(records, total, safePage, safeSize);
    }

    public ArticleDetailResponse getPublishedArticle(Long articleId) {
        Article article = articleMapper.findPublishedById(articleId);
        if (article == null) {
            throw new BusinessException(42000, "article does not exist", HttpStatus.NOT_FOUND);
        }
        List<ChapterSummaryResponse> chapters = chapterService.getPublishedChapters(articleId);
        return ArticleDetailResponse.from(article, chapters);
    }

    public void ensurePublishedArticleExists(Long articleId) {
        if (articleMapper.findPublishedById(articleId) == null) {
            throw new BusinessException(42000, "article does not exist", HttpStatus.NOT_FOUND);
        }
    }
}
