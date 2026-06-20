package com.flowstudy.core.module.submission.mapper;

import com.flowstudy.core.module.submission.entity.Submission;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubmissionMapper {

    @Insert("""
            INSERT INTO fs_submission (
                user_id,
                problem_id,
                language,
                code,
                judge_code,
                submit_mode,
                status,
                score,
                trace_id
            )
            VALUES (
                #{userId},
                #{problemId},
                #{language},
                #{code},
                #{judgeCode},
                #{submitMode},
                #{status},
                #{score},
                #{traceId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Submission submission);

    @Select("""
            SELECT
                s.id,
                s.user_id,
                s.problem_id,
                p.title AS problem_title,
                s.language,
                s.code,
                s.judge_code,
                s.submit_mode,
                s.status,
                s.score,
                s.time_used_ms,
                s.memory_used_kb,
                s.compile_message,
                s.runtime_message,
                s.trace_id,
                s.created_at,
                s.updated_at
            FROM fs_submission s
            JOIN fs_problem p ON p.id = s.problem_id
            WHERE s.id = #{submitId}
              AND s.user_id = #{userId}
            LIMIT 1
            """)
    Submission findByIdAndUserId(@Param("submitId") Long submitId, @Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM fs_submission s
            WHERE s.user_id = #{userId}
              AND (#{problemId} IS NULL OR s.problem_id = #{problemId})
            """)
    long countByUserId(@Param("userId") Long userId, @Param("problemId") Long problemId);

    @Select("""
            SELECT
                s.id,
                s.user_id,
                s.problem_id,
                p.title AS problem_title,
                s.language,
                s.code,
                s.judge_code,
                s.submit_mode,
                s.status,
                s.score,
                s.time_used_ms,
                s.memory_used_kb,
                s.compile_message,
                s.runtime_message,
                s.trace_id,
                s.created_at,
                s.updated_at
            FROM fs_submission s
            JOIN fs_problem p ON p.id = s.problem_id
            WHERE s.user_id = #{userId}
              AND (#{problemId} IS NULL OR s.problem_id = #{problemId})
            ORDER BY s.created_at DESC, s.id DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<Submission> findPageByUserId(
            @Param("userId") Long userId,
            @Param("problemId") Long problemId,
            @Param("limit") int limit,
            @Param("offset") int offset);
}
