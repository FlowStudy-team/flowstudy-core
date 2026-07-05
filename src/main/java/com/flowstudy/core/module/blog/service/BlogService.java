package com.flowstudy.core.module.blog.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.module.blog.dto.CreateBlogRequest;
import com.flowstudy.core.module.blog.dto.UpdateBlogRequest;
import com.flowstudy.core.module.blog.entity.Blog;
import com.flowstudy.core.module.blog.mapper.BlogMapper;
import com.flowstudy.core.module.blog.vo.BlogDetailResponse;
import com.flowstudy.core.module.blog.vo.BlogSummaryResponse;
import com.flowstudy.core.module.blog.vo.ProblemSummaryResponse;
import com.flowstudy.core.module.tutorial.service.TutorialService;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BlogService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BlogMapper blogMapper;
    private final TutorialService tutorialService;

    public BlogService(BlogMapper blogMapper, @Lazy TutorialService tutorialService) {
        this.blogMapper = blogMapper;
        this.tutorialService = tutorialService;
    }

    public List<BlogSummaryResponse> getPublishedBlogs(Long tutorialId) {
        tutorialService.ensurePublishedTutorialExists(tutorialId);
        return blogMapper.findPublishedByTutorialId(tutorialId).stream()
                .map(blog -> BlogSummaryResponse.from(
                        blog,
                        blogMapper.findPublishedProblemIds(blog.getId())))
                .toList();
    }

    public PageResponse<BlogSummaryResponse> getPublishedBlogs(
            Long tutorialId,
            boolean standalone,
            String keyword,
            Integer page,
            Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : Math.min(size, MAX_PAGE_SIZE);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        long total = blogMapper.countPublished(tutorialId, standalone, normalizedKeyword);
        List<BlogSummaryResponse> records = blogMapper.findPublishedPage(
                        tutorialId,
                        standalone,
                        normalizedKeyword,
                        safeSize,
                        (safePage - 1) * safeSize)
                .stream()
                .map(blog -> BlogSummaryResponse.from(
                        blog,
                        blogMapper.findPublishedProblemIds(blog.getId())))
                .toList();
        return new PageResponse<>(records, total, safePage, safeSize);
    }

    public BlogDetailResponse getPublishedBlog(Long blogId) {
        Blog blog = blogMapper.findPublishedById(blogId);
        if (blog == null) {
            throw new BusinessException(42001, "blog does not exist", HttpStatus.NOT_FOUND);
        }
        if (blog.getTutorialId() != null) {
            tutorialService.ensurePublishedTutorialExists(blog.getTutorialId());
        }
        List<ProblemSummaryResponse> problems = blogMapper.findPublishedProblems(blogId);
        Long prevBlogId = blog.getTutorialId() == null ? null : blogMapper.findPrevBlogId(
                blog.getTutorialId(),
                blog.getSortOrder(),
                blog.getId());
        Long nextBlogId = blog.getTutorialId() == null ? null : blogMapper.findNextBlogId(
                blog.getTutorialId(),
                blog.getSortOrder(),
                blog.getId());
        return BlogDetailResponse.from(blog, problems, prevBlogId, nextBlogId);
    }

    public BlogDetailResponse createBlog(Long userId, CreateBlogRequest request) {
        if (request.tutorialId() != null) {
            tutorialService.ensurePublishedTutorialExists(request.tutorialId());
        }
        Blog blog = new Blog();
        blog.setTutorialId(request.tutorialId());
        blog.setAuthorId(userId);
        blog.setTitle(request.title().trim());
        blog.setContentMd(request.contentMd());
        blog.setSummary(request.summary() != null ? request.summary().trim() : null);
        blog.setEstimatedMinutes(request.estimatedMinutes());
        blog.setSortOrder(0);
        blog.setStatus(request.status() != null && !request.status().isBlank()
                ? request.status().trim() : "PUBLISHED");
        blogMapper.insert(blog);
        return getPublishedBlog(blog.getId());
    }

    public BlogDetailResponse updateBlog(Long userId, Long blogId, UpdateBlogRequest request) {
        Blog blog = blogMapper.findById(blogId);
        if (blog == null) {
            throw new BusinessException(42001, "blog does not exist", HttpStatus.NOT_FOUND);
        }
        if (!userId.equals(blog.getAuthorId())) {
            throw new BusinessException(42002, "permission denied", HttpStatus.FORBIDDEN);
        }
        if (request.tutorialId() != null) {
            tutorialService.ensurePublishedTutorialExists(request.tutorialId());
        }
        blog.setTutorialId(request.tutorialId());
        blog.setTitle(request.title().trim());
        blog.setContentMd(request.contentMd());
        blog.setSummary(request.summary() != null ? request.summary().trim() : null);
        blog.setEstimatedMinutes(request.estimatedMinutes());
        blog.setStatus(request.status() != null && !request.status().isBlank()
                ? request.status().trim() : blog.getStatus());
        blogMapper.update(blog);
        return getPublishedBlog(blog.getId());
    }
}
