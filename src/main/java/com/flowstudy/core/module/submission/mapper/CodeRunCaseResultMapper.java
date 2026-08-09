package com.flowstudy.core.module.submission.mapper;

import com.flowstudy.core.module.submission.entity.JudgeCaseResult;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CodeRunCaseResultMapper {

    @Select("""
            SELECT
                id,
                run_id AS submission_id,
                testcase_id,
                case_index,
                status,
                time_used_ms,
                memory_used_kb,
                input_text,
                actual_output,
                expected_output,
                error_message,
                created_at
            FROM fs_code_run_case_result
            WHERE run_id = #{runId}
            ORDER BY case_index ASC, id ASC
            """)
    List<JudgeCaseResult> findByRunId(@Param("runId") Long runId);
}
