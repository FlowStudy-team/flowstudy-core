package com.flowstudy.core.module.chapter.controller;

import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.chapter.service.ChapterService;
import com.flowstudy.core.module.chapter.vo.ChapterDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chapters")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping("/{chapterId}")
    public Result<ChapterDetailResponse> getChapter(@PathVariable Long chapterId) {
        return Result.success(chapterService.getPublishedChapter(chapterId));
    }
}
