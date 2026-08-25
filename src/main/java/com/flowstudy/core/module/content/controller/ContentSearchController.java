package com.flowstudy.core.module.content.controller;

import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.content.service.ContentSearchService;
import com.flowstudy.core.module.content.vo.ContentSearchResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
public class ContentSearchController {
    private final ContentSearchService service;

    public ContentSearchController(ContentSearchService service) {
        this.service = service;
    }

    @GetMapping
    public Result<ContentSearchResponse> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String searchAfter) {
        return Result.success(service.search(keyword, size, searchAfter));
    }
}
