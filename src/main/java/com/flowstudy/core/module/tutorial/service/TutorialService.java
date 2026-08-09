package com.flowstudy.core.module.tutorial.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.module.blog.service.BlogService;
import com.flowstudy.core.module.blog.vo.BlogSummaryResponse;
import com.flowstudy.core.module.tutorial.entity.Tutorial;
import com.flowstudy.core.module.tutorial.mapper.TutorialMapper;
import com.flowstudy.core.module.tutorial.vo.TutorialDetailResponse;
import com.flowstudy.core.module.tutorial.vo.TutorialSummaryResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TutorialService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TutorialMapper tutorialMapper;
    private final BlogService blogService;

    public TutorialService(TutorialMapper tutorialMapper, BlogService blogService) {
        this.tutorialMapper = tutorialMapper;
        this.blogService = blogService;
    }

    public PageResponse<TutorialSummaryResponse> getPublishedTutorials(Integer page, Integer size, String keyword) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : Math.min(size, MAX_PAGE_SIZE);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        long total = tutorialMapper.countPublished(normalizedKeyword);
        List<TutorialSummaryResponse> records = tutorialMapper.findPublishedPage(
                        normalizedKeyword,
                        safeSize,
                        (safePage - 1) * safeSize)
                .stream()
                .map(TutorialSummaryResponse::from)
                .toList();
        return new PageResponse<>(records, total, safePage, safeSize);
    }

    public TutorialDetailResponse getPublishedTutorial(Long tutorialId) {
        Tutorial tutorial = tutorialMapper.findPublishedById(tutorialId);
        if (tutorial == null) {
            throw new BusinessException(42000, "tutorial does not exist", HttpStatus.NOT_FOUND);
        }
        List<BlogSummaryResponse> blogs = blogService.getPublishedBlogs(tutorialId);
        return TutorialDetailResponse.from(tutorial, blogs);
    }

    public void ensurePublishedTutorialExists(Long tutorialId) {
        if (tutorialMapper.findPublishedById(tutorialId) == null) {
            throw new BusinessException(42000, "tutorial does not exist", HttpStatus.NOT_FOUND);
        }
    }
}
