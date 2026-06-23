package com.flowstudy.core.module.problem;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
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
class ProblemControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedDatabase() {
        jdbcTemplate.update("DELETE FROM fs_code_template");
        jdbcTemplate.update("DELETE FROM fs_problem_testcase");
        jdbcTemplate.update("DELETE FROM fs_problem");
        jdbcTemplate.update("DELETE FROM fs_blog");
        jdbcTemplate.update("DELETE FROM fs_tutorial");
        jdbcTemplate.update("DELETE FROM sys_user");

        jdbcTemplate.update("""
                INSERT INTO fs_tutorial (id, title, status, deleted)
                VALUES (1, 'Java 并发', 'PUBLISHED', 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO fs_blog (id, tutorial_id, title, content_md, sort_order, status, deleted)
                VALUES (10, 1, '线程池', 'Blog', 1, 'PUBLISHED', 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO fs_problem (
                    id, blog_id, title, description_md, difficulty, input_description, output_description,
                    support_languages, time_limit_ms, memory_limit_mb, status, submit_count,
                    accepted_count, sort_order, deleted
                )
                VALUES
                    (100, 10, '两数之和', '## 题目描述', 'EASY', '输入 n 和数组', '输出两个下标',
                     'java,cpp,go,python', 1000, 256, 'PUBLISHED', 180, 100, 1, 0),
                    (101, 10, '任务队列', '## 队列调度', 'MEDIUM', '输入任务', '输出顺序',
                     'java,cpp', 2000, 512, 'PUBLISHED', 10, 5, 2, 0),
                    (102, 10, '草稿题目', 'hidden', 'EASY', 'hidden', 'hidden',
                     'java', 1000, 256, 'DRAFT', 0, 0, 3, 0),
                    (103, 10, '删除题目', 'hidden', 'EASY', 'hidden', 'hidden',
                     'java', 1000, 256, 'PUBLISHED', 0, 0, 4, 1)
                """);
        jdbcTemplate.update("""
                INSERT INTO fs_problem_testcase (
                    id, problem_id, input_text, expected_output, is_sample, sort_order, deleted
                )
                VALUES
                    (1, 100, '4
2 7 11 15
9
', '0 1
', 1, 1, 0),
                    (2, 100, '3
3 2 4
6
', '1 2
', 0, 2, 0),
                    (3, 101, '1
', '1
', 1, 1, 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO fs_code_template (problem_id, language, template_code, deleted)
                VALUES
                    (100, 'java', 'public class Main {}', 0),
                    (100, 'cpp', '#include <bits/stdc++.h>', 0),
                    (102, 'java', 'hidden', 0)
                """);
    }

    @Test
    void listPublishedProblemsWithFiltersWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/problems?blogId=10&difficulty=easy&keyword=两数&page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records", hasSize(1)))
                .andExpect(jsonPath("$.data.records[0].id").value(100))
                .andExpect(jsonPath("$.data.records[0].blogId").value(10))
                .andExpect(jsonPath("$.data.records[0].difficulty").value("EASY"))
                .andExpect(jsonPath("$.data.records[0].acceptedCount").value(100))
                .andExpect(jsonPath("$.data.records[0].submitCount").value(180))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void getProblemDetailReturnsSampleCasesOnly() throws Exception {
        mockMvc.perform(get("/api/v1/problems/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.title").value("两数之和"))
                .andExpect(jsonPath("$.data.descriptionMd").value("## 题目描述"))
                .andExpect(jsonPath("$.data.inputDescription").value("输入 n 和数组"))
                .andExpect(jsonPath("$.data.outputDescription").value("输出两个下标"))
                .andExpect(jsonPath("$.data.sampleCases", hasSize(1)))
                .andExpect(jsonPath("$.data.sampleCases[0].input").value("4\n2 7 11 15\n9\n"))
                .andExpect(jsonPath("$.data.sampleCases[0].output").value("0 1\n"))
                .andExpect(jsonPath("$.data.supportLanguages", hasSize(4)))
                .andExpect(jsonPath("$.data.supportLanguages[0]").value("java"))
                .andExpect(jsonPath("$.data.timeLimitMs").value(1000))
                .andExpect(jsonPath("$.data.memoryLimitMb").value(256));
    }

    @Test
    void getCodeTemplateNormalizesLanguage() throws Exception {
        mockMvc.perform(get("/api/v1/problems/100/template?language=Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.problemId").value(100))
                .andExpect(jsonPath("$.data.language").value("java"))
                .andExpect(jsonPath("$.data.code").value("public class Main {}"));
    }

    @Test
    void rejectMissingUnpublishedOrMissingTemplate() throws Exception {
        mockMvc.perform(get("/api/v1/problems/102"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(42002));

        mockMvc.perform(get("/api/v1/problems/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(42002));

        mockMvc.perform(get("/api/v1/problems/102/template?language=java"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(42002));

        mockMvc.perform(get("/api/v1/problems/100/template?language=python"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(42006));
    }
}
