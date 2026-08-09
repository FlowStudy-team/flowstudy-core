package com.flowstudy.core.module.tutorial.controller;

import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.blog.service.BlogService;
import com.flowstudy.core.module.blog.vo.BlogSummaryResponse;
import com.flowstudy.core.module.tutorial.service.TutorialService;
import com.flowstudy.core.module.tutorial.vo.TutorialDetailResponse;
import com.flowstudy.core.module.tutorial.vo.TutorialSummaryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tutorials")
public class TutorialController {

    private final TutorialService tutorialService;
    private final BlogService blogService;

    public TutorialController(TutorialService tutorialService, BlogService blogService) {
        this.tutorialService = tutorialService;
        this.blogService = blogService;
    }

    @GetMapping
    public Result<PageResponse<TutorialSummaryResponse>> listTutorials(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(tutorialService.getPublishedTutorials(page, size == null ? pageSize : size, keyword));
    }

    @GetMapping("/{tutorialId}")
    public Result<TutorialDetailResponse> getTutorial(@PathVariable Long tutorialId) {
        return Result.success(tutorialService.getPublishedTutorial(tutorialId));
    }

    @GetMapping("/{tutorialId}/blogs")
    public Result<List<BlogSummaryResponse>> listBlogs(@PathVariable Long tutorialId) {
        return Result.success(blogService.getPublishedBlogs(tutorialId));
    }
}
