package com.flowstudy.core.module.chapter.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.module.chapter.entity.Chapter;
import com.flowstudy.core.module.chapter.mapper.ChapterMapper;
import com.flowstudy.core.module.chapter.vo.ChapterDetailResponse;
import com.flowstudy.core.module.chapter.vo.ChapterSummaryResponse;
import com.flowstudy.core.module.chapter.vo.ProblemSummaryResponse;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ChapterService {

    private final ChapterMapper chapterMapper;
    private final com.flowstudy.core.module.article.service.ArticleService articleService;

    public ChapterService(
            ChapterMapper chapterMapper,
            @Lazy com.flowstudy.core.module.article.service.ArticleService articleService) {
        this.chapterMapper = chapterMapper;
        this.articleService = articleService;
    }

    public List<ChapterSummaryResponse> getPublishedChapters(Long articleId) {
        articleService.ensurePublishedArticleExists(articleId);
        return chapterMapper.findPublishedByArticleId(articleId).stream()
                .map(chapter -> ChapterSummaryResponse.from(
                        chapter,
                        chapterMapper.findPublishedProblemIds(chapter.getId())))
                .toList();
    }

    public ChapterDetailResponse getPublishedChapter(Long chapterId) {
        Chapter chapter = chapterMapper.findPublishedById(chapterId);
        if (chapter == null) {
            throw new BusinessException(42001, "chapter does not exist", HttpStatus.NOT_FOUND);
        }
        articleService.ensurePublishedArticleExists(chapter.getArticleId());
        List<ProblemSummaryResponse> problems = chapterMapper.findPublishedProblems(chapterId);
        Long prevChapterId = chapterMapper.findPrevChapterId(
                chapter.getArticleId(),
                chapter.getSortOrder(),
                chapter.getId());
        Long nextChapterId = chapterMapper.findNextChapterId(
                chapter.getArticleId(),
                chapter.getSortOrder(),
                chapter.getId());
        return ChapterDetailResponse.from(chapter, problems, prevChapterId, nextChapterId);
    }
}
