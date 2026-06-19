package com.flowstudy.core.module.submission;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubmissionControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedDatabase() {
        jdbcTemplate.update("DELETE FROM fs_submission");
        jdbcTemplate.update("DELETE FROM fs_code_template");
        jdbcTemplate.update("DELETE FROM fs_problem_testcase");
        jdbcTemplate.update("DELETE FROM fs_problem");
        jdbcTemplate.update("DELETE FROM fs_chapter");
        jdbcTemplate.update("DELETE FROM fs_article");
        jdbcTemplate.update("DELETE FROM sys_user");

        jdbcTemplate.update("""
                INSERT INTO fs_article (id, title, status, deleted)
                VALUES (1, 'Java Basics', 'PUBLISHED', 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO fs_chapter (id, article_id, title, content_md, sort_order, status, deleted)
                VALUES (10, 1, 'Arrays', 'chapter', 1, 'PUBLISHED', 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO fs_problem (
                    id, chapter_id, title, description_md, difficulty, input_description, output_description,
                    support_languages, time_limit_ms, memory_limit_mb, status, submit_count,
                    accepted_count, sort_order, deleted
                )
                VALUES
                    (100, 10, 'Two Sum', 'description', 'EASY', 'input', 'output',
                     'java,cpp', 1000, 256, 'PUBLISHED', 0, 0, 1, 0),
                    (101, 10, 'Draft Problem', 'hidden', 'EASY', 'input', 'output',
                     'java', 1000, 256, 'DRAFT', 0, 0, 2, 0)
                """);
    }

    @Test
    void rejectSubmissionWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/problems/100/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"java","code":"public class Main {}"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void createPendingSubmissionAndReadDetail() throws Exception {
        String accessToken = registerAndLogin("alice", "alice@example.com");

        String response = mockMvc.perform(post("/api/v1/problems/100/submissions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"Java","code":"public class Main { public static void main(String[] args) {} }"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.submitId").isNumber())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long submitId = objectMapper.readTree(response).path("data").path("submitId").asLong();
        String storedLanguage = jdbcTemplate.queryForObject(
                "SELECT language FROM fs_submission WHERE id = ?", String.class, submitId);
        String storedTraceId = jdbcTemplate.queryForObject(
                "SELECT trace_id FROM fs_submission WHERE id = ?", String.class, submitId);
        org.junit.jupiter.api.Assertions.assertEquals("java", storedLanguage);
        org.junit.jupiter.api.Assertions.assertNotNull(storedTraceId);
        org.junit.jupiter.api.Assertions.assertFalse(storedTraceId.isBlank());

        mockMvc.perform(get("/api/v1/submissions/" + submitId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.submitId").value(submitId))
                .andExpect(jsonPath("$.data.problemId").value(100))
                .andExpect(jsonPath("$.data.problemTitle").value("Two Sum"))
                .andExpect(jsonPath("$.data.language").value("java"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.score").value(0))
                .andExpect(jsonPath("$.data.caseResults", hasSize(0)));
    }

    @Test
    void listMySubmissionsWithProblemFilter() throws Exception {
        String aliceToken = registerAndLogin("alice", "alice@example.com");
        String bobToken = registerAndLogin("bob", "bob@example.com");

        createSubmission(aliceToken, 100, "java");
        createSubmission(aliceToken, 100, "cpp");
        createSubmission(bobToken, 100, "java");

        mockMvc.perform(get("/api/v1/submissions/my?problemId=100&page=1&size=10")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records", hasSize(2)))
                .andExpect(jsonPath("$.data.records[0].problemTitle").value("Two Sum"))
                .andExpect(jsonPath("$.data.records[0].status").value("PENDING"));
    }

    @Test
    void rejectUnpublishedProblemUnsupportedLanguageAndForeignSubmission() throws Exception {
        String aliceToken = registerAndLogin("alice", "alice@example.com");
        String bobToken = registerAndLogin("bob", "bob@example.com");

        mockMvc.perform(post("/api/v1/problems/101/submissions")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"java","code":"public class Main {}"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(42002));

        mockMvc.perform(post("/api/v1/problems/100/submissions")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"python","code":"print(1)"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));

        long submitId = createSubmission(aliceToken, 100, "java");
        mockMvc.perform(get("/api/v1/submissions/" + submitId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(43000));
    }

    private String registerAndLogin(String username, String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","password":"password123"}
                                """.formatted(username, email)))
                .andExpect(status().isOk());

        String loginBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"%s","password":"password123"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginBody);
        return loginJson.path("data").path("accessToken").asText();
    }

    private long createSubmission(String accessToken, long problemId, String language) throws Exception {
        String response = mockMvc.perform(post("/api/v1/problems/" + problemId + "/submissions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"%s","code":"int main() { return 0; }"}
                                """.formatted(language)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("submitId").asLong();
    }
}
