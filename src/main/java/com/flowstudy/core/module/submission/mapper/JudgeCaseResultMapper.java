package com.flowstudy.core.module.submission.mapper;

import com.flowstudy.core.module.submission.entity.JudgeCaseResult;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JudgeCaseResultMapper {

    @Select("""
            SELECT
                id,
                submission_id,
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
            FROM fs_judge_case_result
            WHERE submission_id = #{submissionId}
              AND status <> 'ACCEPTED'
            ORDER BY case_index ASC, id ASC
            LIMIT 1
            """)
    List<JudgeCaseResult> findBySubmissionId(@Param("submissionId") Long submissionId);
}
