package com.flowstudy.core.module.problem.mapper;

import com.flowstudy.core.module.problem.entity.CodeTemplate;
import com.flowstudy.core.module.problem.entity.Problem;
import com.flowstudy.core.module.problem.entity.ProblemSampleCase;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProblemMapper {

    @Select("""
            SELECT COUNT(1)
            FROM fs_problem p
            WHERE p.deleted = 0
              AND p.status = 'PUBLISHED'
              AND (#{blogId} IS NULL OR p.blog_id = #{blogId})
              AND (#{difficulty} IS NULL OR p.difficulty = #{difficulty})
              AND (#{keyword} IS NULL OR p.title LIKE CONCAT('%', #{keyword}, '%')
                   OR p.description_md LIKE CONCAT('%', #{keyword}, '%'))
            """)
    long countPublished(
            @Param("blogId") Long blogId,
            @Param("difficulty") String difficulty,
            @Param("keyword") String keyword);

    @Select("""
            SELECT
                id,
                blog_id,
                title,
                description_md,
                difficulty,
                input_description,
                output_description,
                support_languages,
                time_limit_ms,
                memory_limit_mb,
                status,
                submit_count,
                accepted_count,
                sort_order,
                created_at,
                updated_at
            FROM fs_problem p
            WHERE p.deleted = 0
              AND p.status = 'PUBLISHED'
              AND (#{blogId} IS NULL OR p.blog_id = #{blogId})
              AND (#{difficulty} IS NULL OR p.difficulty = #{difficulty})
              AND (#{keyword} IS NULL OR p.title LIKE CONCAT('%', #{keyword}, '%')
                   OR p.description_md LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY p.sort_order ASC, p.id ASC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<Problem> findPublishedPage(
            @Param("blogId") Long blogId,
            @Param("difficulty") String difficulty,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Select("""
            SELECT
                id,
                blog_id,
                title,
                description_md,
                difficulty,
                input_description,
                output_description,
                support_languages,
                time_limit_ms,
                memory_limit_mb,
                status,
                submit_count,
                accepted_count,
                sort_order,
                created_at,
                updated_at
            FROM fs_problem
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND id = #{id}
            LIMIT 1
            """)
    Problem findPublishedById(@Param("id") Long id);

    @Select("""
            SELECT id, problem_id, input_text, expected_output, sort_order
            FROM fs_problem_testcase
            WHERE deleted = 0
              AND is_sample = 1
              AND problem_id = #{problemId}
            ORDER BY sort_order ASC, id ASC
            """)
    List<ProblemSampleCase> findSampleCases(@Param("problemId") Long problemId);

    @Select("""
            SELECT id, problem_id, input_text, expected_output, sort_order
            FROM fs_problem_testcase
            WHERE deleted = 0
              AND problem_id = #{problemId}
            ORDER BY sort_order ASC, id ASC
            """)
    List<ProblemSampleCase> findJudgeCases(@Param("problemId") Long problemId);

    @Select("""
            SELECT problem_id, language, template_code, judge_wrapper_code
            FROM fs_code_template
            WHERE deleted = 0
              AND problem_id = #{problemId}
              AND language = #{language}
            LIMIT 1
            """)
    CodeTemplate findCodeTemplate(
            @Param("problemId") Long problemId,
            @Param("language") String language);

    @Update("""
            UPDATE fs_problem
            SET submit_count = submit_count + 1
            WHERE id = #{problemId}
            """)
    int incrementSubmitCount(@Param("problemId") Long problemId);
}
