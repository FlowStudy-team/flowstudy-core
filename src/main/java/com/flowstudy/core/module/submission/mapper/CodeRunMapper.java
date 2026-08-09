package com.flowstudy.core.module.submission.mapper;

import com.flowstudy.core.module.submission.entity.CodeRun;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CodeRunMapper {

    @Insert("""
            INSERT INTO fs_code_run (
                user_id,
                problem_id,
                language,
                code,
                judge_code,
                submit_mode,
                status,
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
                #{traceId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CodeRun run);

    @Select("""
            SELECT
                r.id,
                r.user_id,
                r.problem_id,
                p.title AS problem_title,
                r.language,
                r.code,
                r.judge_code,
                r.submit_mode,
                r.status,
                r.time_used_ms,
                r.memory_used_kb,
                r.compile_message,
                r.runtime_message,
                r.trace_id,
                r.created_at,
                r.updated_at
            FROM fs_code_run r
            JOIN fs_problem p ON p.id = r.problem_id
            WHERE r.id = #{runId}
              AND r.user_id = #{userId}
            LIMIT 1
            """)
    CodeRun findByIdAndUserId(@Param("runId") Long runId, @Param("userId") Long userId);
}
