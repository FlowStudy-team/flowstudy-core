package com.flowstudy.core.module.blog.controller;

import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.blog.dto.CreateBlogRequest;
import com.flowstudy.core.module.blog.dto.UpdateBlogRequest;
import com.flowstudy.core.module.blog.service.BlogService;
import com.flowstudy.core.module.blog.vo.BlogDetailResponse;
import com.flowstudy.core.module.blog.vo.BlogSummaryResponse;
import com.flowstudy.core.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/blogs")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public Result<PageResponse<BlogSummaryResponse>> listBlogs(
            @RequestParam(required = false) Long tutorialId,
            @RequestParam(required = false) Boolean standalone,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(blogService.getPublishedBlogs(
                tutorialId,
                Boolean.TRUE.equals(standalone),
                keyword,
                page,
                size == null ? pageSize : size));
    }

    @GetMapping("/{blogId}")
    public Result<BlogDetailResponse> getBlog(@PathVariable Long blogId) {
        return Result.success(blogService.getPublishedBlog(blogId));
    }

    @PostMapping
    public Result<BlogDetailResponse> createBlog(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateBlogRequest request) {
        return Result.success(blogService.createBlog(user.id(), request));
    }

    @PutMapping("/{blogId}")
    public Result<BlogDetailResponse> updateBlog(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long blogId,
            @Valid @RequestBody UpdateBlogRequest request) {
        return Result.success(blogService.updateBlog(user.id(), blogId, request));
    }
}
