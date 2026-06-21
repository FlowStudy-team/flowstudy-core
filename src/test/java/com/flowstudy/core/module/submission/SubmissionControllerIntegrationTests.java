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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

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

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void seedDatabase() {
        jdbcTemplate.update("DELETE FROM fs_judge_case_result");
        jdbcTemplate.update("DELETE FROM fs_code_run_case_result");
        jdbcTemplate.update("DELETE FROM fs_code_run");
        jdbcTemplate.update("DELETE FROM fs_submission");
        jdbcTemplate.update("DELETE FROM fs_code_template");
        jdbcTemplate.update("DELETE FROM fs_problem_testcase");
        jdbcTemplate.update("DELETE FROM fs_problem");
        jdbcTemplate.update("DELETE FROM fs_chapter");
        jdbcTemplate.update("DELETE FROM fs_article");
        jdbcTemplate.update("DELETE FROM sys_user");
        reset(rabbitTemplate);

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
        jdbcTemplate.update("""
                INSERT INTO fs_problem_testcase (
                    id, problem_id, input_text, expected_output, is_sample, sort_order, deleted
                )
                VALUES
                    (1, 100, '1 2\\n', '3\\n', 1, 1, 0),
                    (2, 100, '2 3\\n', '5\\n', 0, 2, 0)
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
        String storedCode = jdbcTemplate.queryForObject(
                "SELECT code FROM fs_submission WHERE id = ?", String.class, submitId);
        String storedJudgeCode = jdbcTemplate.queryForObject(
                "SELECT judge_code FROM fs_submission WHERE id = ?", String.class, submitId);
        String storedSubmitMode = jdbcTemplate.queryForObject(
                "SELECT submit_mode FROM fs_submission WHERE id = ?", String.class, submitId);
        Long submitCount = jdbcTemplate.queryForObject(
                "SELECT submit_count FROM fs_problem WHERE id = 100", Long.class);
        Assertions.assertEquals("java", storedLanguage);
        Assertions.assertEquals("public class Main { public static void main(String[] args) {} }", storedCode);
        Assertions.assertEquals(storedCode, storedJudgeCode);
        Assertions.assertEquals("FULL_PROGRAM", storedSubmitMode);
        Assertions.assertEquals(1L, submitCount);
        Assertions.assertNotNull(storedTraceId);
        Assertions.assertFalse(storedTraceId.isBlank());
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("submission_queue"), messageCaptor.capture());
        JsonNode messageJson = objectMapper.readTree(messageCaptor.getValue());
        Assertions.assertEquals(submitId, messageJson.path("submission_id").asLong());
        Assertions.assertEquals(100, messageJson.path("problem_id").asLong());
        Assertions.assertEquals("java", messageJson.path("language").asText());
        Assertions.assertEquals("FULL_PROGRAM", messageJson.path("submit_mode").asText());
        Assertions.assertEquals(storedCode, messageJson.path("code").asText());
        Assertions.assertEquals(2, messageJson.path("testcases").size());

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
    void readSubmissionDetailReturnsOnlyFailedCaseWithInput() throws Exception {
        String accessToken = registerAndLogin("alice", "alice@example.com");
        long submitId = createSubmission(accessToken, 100, "java");
        jdbcTemplate.update("""
                UPDATE fs_submission
                SET status = 'WRONG_ANSWER', time_used_ms = 11, memory_used_kb = 256
                WHERE id = ?
                """, submitId);
        jdbcTemplate.update("""
                INSERT INTO fs_judge_case_result (
                    submission_id, testcase_id, case_index, status, time_used_ms, memory_used_kb,
                    input_text, actual_output, expected_output, error_message
                )
                VALUES (?, 1, 1, 'ACCEPTED', 3, 128, ?, ?, ?, NULL)
                """, submitId, "1 2\n", "3\n", "3\n");
        jdbcTemplate.update("""
                INSERT INTO fs_judge_case_result (
                    submission_id, testcase_id, case_index, status, time_used_ms, memory_used_kb,
                    input_text, actual_output, expected_output, error_message
                )
                VALUES (?, 2, 2, 'WRONG_ANSWER', 8, 256, ?, ?, ?, 'Wrong answer on testcase 2')
                """, submitId, "2 3\n", "4\n", "5\n");

        mockMvc.perform(get("/api/v1/submissions/" + submitId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WRONG_ANSWER"))
                .andExpect(jsonPath("$.data.caseResults", hasSize(1)))
                .andExpect(jsonPath("$.data.caseResults[0].caseIndex").value(2))
                .andExpect(jsonPath("$.data.caseResults[0].status").value("WRONG_ANSWER"))
                .andExpect(jsonPath("$.data.caseResults[0].input").value("2 3\n"))
                .andExpect(jsonPath("$.data.caseResults[0].expectedOutput").value("5\n"))
                .andExpect(jsonPath("$.data.caseResults[0].actualOutput").value("4\n"))
                .andExpect(jsonPath("$.data.caseResults[0].errorMessage").value("Wrong answer on testcase 2"));
    }

    @Test
    void createCodeRunUsesRequestCasesAndDoesNotCreateSubmission() throws Exception {
        String accessToken = registerAndLogin("alice", "alice@example.com");

        String response = mockMvc.perform(post("/api/v1/problems/100/runs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "language":"Java",
                                  "code":"public class Main { public static void main(String[] args) {} }",
                                  "testCases":[
                                    {"input":"1 2\\n","expectedOutput":"3\\n"},
                                    {"input":"5 7\\n","expectedOutput":"12\\n"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.runId").isNumber())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long runId = objectMapper.readTree(response).path("data").path("runId").asLong();
        Long submissionCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM fs_submission", Long.class);
        Long submitCount = jdbcTemplate.queryForObject(
                "SELECT submit_count FROM fs_problem WHERE id = 100", Long.class);
        String storedMode = jdbcTemplate.queryForObject(
                "SELECT submit_mode FROM fs_code_run WHERE id = ?", String.class, runId);
        Assertions.assertEquals(0L, submissionCount);
        Assertions.assertEquals(0L, submitCount);
        Assertions.assertEquals("FULL_PROGRAM", storedMode);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("submission_queue"), messageCaptor.capture());
        JsonNode messageJson = objectMapper.readTree(messageCaptor.getValue());
        Assertions.assertEquals("RUN", messageJson.path("task_type").asText());
        Assertions.assertEquals(runId, messageJson.path("run_id").asLong());
        Assertions.assertTrue(messageJson.path("submission_id").isNull());
        Assertions.assertEquals("java", messageJson.path("language").asText());
        Assertions.assertEquals(2, messageJson.path("testcases").size());
        Assertions.assertTrue(messageJson.path("testcases").get(0).path("testcase_id").isNull());
        Assertions.assertEquals(1, messageJson.path("testcases").get(0).path("case_index").asInt());
        Assertions.assertEquals("1 2\n", messageJson.path("testcases").get(0).path("input").asText());
        Assertions.assertEquals("3\n", messageJson.path("testcases").get(0).path("expected_output").asText());
        Assertions.assertEquals(2, messageJson.path("testcases").get(1).path("case_index").asInt());
        Assertions.assertEquals("5 7\n", messageJson.path("testcases").get(1).path("input").asText());
        Assertions.assertEquals("12\n", messageJson.path("testcases").get(1).path("expected_output").asText());

        mockMvc.perform(get("/api/v1/runs/" + runId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runId").value(runId))
                .andExpect(jsonPath("$.data.problemId").value(100))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.caseResults", hasSize(0)));
    }

    @Test
    void wrapFunctionSubmissionBeforePublishingJudgeTask() throws Exception {
        String accessToken = registerAndLogin("alice", "alice@example.com");
        jdbcTemplate.update("""
                INSERT INTO fs_code_template (problem_id, language, template_code, judge_wrapper_code, deleted)
                VALUES (?, ?, ?, ?, 0)
                """,
                100,
                "java",
                "class Solution { int add(int a, int b) { return 0; } }",
                """
                import java.util.*;

                {{USER_CODE}}

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int a = sc.nextInt();
                        int b = sc.nextInt();
                        System.out.println(new Solution().add(a, b));
                    }
                }
                """);

        String rawCode = "class Solution { int add(int a, int b) { return a + b; } }";
        String response = mockMvc.perform(post("/api/v1/problems/100/submissions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"java","code":"%s"}
                                """.formatted(rawCode)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long submitId = objectMapper.readTree(response).path("data").path("submitId").asLong();
        String storedCode = jdbcTemplate.queryForObject(
                "SELECT code FROM fs_submission WHERE id = ?", String.class, submitId);
        String storedJudgeCode = jdbcTemplate.queryForObject(
                "SELECT judge_code FROM fs_submission WHERE id = ?", String.class, submitId);
        String storedSubmitMode = jdbcTemplate.queryForObject(
                "SELECT submit_mode FROM fs_submission WHERE id = ?", String.class, submitId);
        Assertions.assertEquals(rawCode, storedCode);
        Assertions.assertEquals("TEMPLATE_WRAPPED", storedSubmitMode);
        Assertions.assertTrue(storedJudgeCode.contains(rawCode));
        Assertions.assertTrue(storedJudgeCode.contains("public class Main"));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("submission_queue"), messageCaptor.capture());
        JsonNode messageJson = objectMapper.readTree(messageCaptor.getValue());
        Assertions.assertEquals("TEMPLATE_WRAPPED", messageJson.path("submit_mode").asText());
        Assertions.assertEquals(storedJudgeCode, messageJson.path("code").asText());
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
