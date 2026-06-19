package com.flowstudy.core.module.article;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArticleControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedDatabase() {
        jdbcTemplate.update("DELETE FROM fs_problem");
        jdbcTemplate.update("DELETE FROM fs_chapter");
        jdbcTemplate.update("DELETE FROM fs_article");
        jdbcTemplate.update("DELETE FROM sys_user");

        jdbcTemplate.update("""
                INSERT INTO sys_user (id, username, email, password_hash, nickname, role, status, deleted)
                VALUES (1, 'admin', 'admin@example.com', 'hash', 'Admin', 'ADMIN', 1, 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO fs_article
                    (id, title, summary, cover_url, author_id, status, view_count, like_count, sort_order, published_at, deleted)
                VALUES
                    (1, 'Java 并发编程入门', '线程池和并发任务调度', NULL, 1, 'PUBLISHED', 10, 2, 1, CURRENT_TIMESTAMP, 0),
                    (2, '草稿文章', '不应该公开展示', NULL, 1, 'DRAFT', 0, 0, 2, NULL, 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO fs_chapter
                    (id, article_id, title, content_md, sort_order, estimated_minutes, status, deleted)
                VALUES
                    (10, 1, '线程池基础', '## 线程池基础
正文', 1, 15, 'PUBLISHED', 0),
                    (11, 1, '任务调度', '## 任务调度
正文', 2, 10, 'PUBLISHED', 0),
                    (12, 1, '未发布章节', 'hidden', 3, 5, 'DRAFT', 0),
                    (20, 2, '草稿章节', 'hidden', 1, 5, 'PUBLISHED', 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO fs_problem
                    (id, chapter_id, title, description_md, difficulty, status, sort_order, deleted)
                VALUES
                    (100, 10, '两数之和', 'desc', 'EASY', 'PUBLISHED', 1, 0),
                    (101, 10, '隐藏题目', 'desc', 'MEDIUM', 'DRAFT', 2, 0),
                    (102, 11, '任务队列', 'desc', 'MEDIUM', 'PUBLISHED', 1, 0)
                """);
    }

    @Test
    void listPublishedArticlesWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/articles?page=1&size=10&keyword=Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records", hasSize(1)))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].chapterCount").value(2))
                .andExpect(jsonPath("$.data.records[0].problemCount").value(2))
                .andExpect(jsonPath("$.data.records[0].authorName").value("Admin"));
    }

    @Test
    void getArticleDetailWithChapters() throws Exception {
        mockMvc.perform(get("/api/v1/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Java 并发编程入门"))
                .andExpect(jsonPath("$.data.author.nickname").value("Admin"))
                .andExpect(jsonPath("$.data.chapters", hasSize(2)))
                .andExpect(jsonPath("$.data.chapters[0].id").value(10))
                .andExpect(jsonPath("$.data.chapters[0].problemIds[0]").value("100"));
    }

    @Test
    void getArticleChaptersAndChapterDetail() throws Exception {
        mockMvc.perform(get("/api/v1/articles/1/chapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(10));

        mockMvc.perform(get("/api/v1/chapters/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.articleId").value(1))
                .andExpect(jsonPath("$.data.contentMd").value("## 线程池基础\n正文"))
                .andExpect(jsonPath("$.data.markdown").value("## 线程池基础\n正文"))
                .andExpect(jsonPath("$.data.problemIds[0]").value("100"))
                .andExpect(jsonPath("$.data.prevChapterId").doesNotExist())
                .andExpect(jsonPath("$.data.nextChapterId").value(11));
    }

    @Test
    void rejectMissingOrUnpublishedContent() throws Exception {
        mockMvc.perform(get("/api/v1/articles/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(42000));

        mockMvc.perform(get("/api/v1/chapters/20"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(42000));

        mockMvc.perform(get("/api/v1/chapters/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(42001));
    }
}
