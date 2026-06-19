package com.flowstudy.core.module.article.controller;

import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.article.service.ArticleService;
import com.flowstudy.core.module.article.vo.ArticleDetailResponse;
import com.flowstudy.core.module.article.vo.ArticleSummaryResponse;
import com.flowstudy.core.module.chapter.service.ChapterService;
import com.flowstudy.core.module.chapter.vo.ChapterSummaryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final ChapterService chapterService;

    public ArticleController(ArticleService articleService, ChapterService chapterService) {
        this.articleService = articleService;
        this.chapterService = chapterService;
    }

    @GetMapping
    public Result<PageResponse<ArticleSummaryResponse>> listArticles(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(articleService.getPublishedArticles(page, size == null ? pageSize : size, keyword));
    }

    @GetMapping("/{articleId}")
    public Result<ArticleDetailResponse> getArticle(@PathVariable Long articleId) {
        return Result.success(articleService.getPublishedArticle(articleId));
    }

    @GetMapping("/{articleId}/chapters")
    public Result<List<ChapterSummaryResponse>> listChapters(@PathVariable Long articleId) {
        return Result.success(chapterService.getPublishedChapters(articleId));
    }
}
