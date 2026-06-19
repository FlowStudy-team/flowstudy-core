package com.flowstudy.core.module.chapter.mapper;

import com.flowstudy.core.module.chapter.entity.Chapter;
import com.flowstudy.core.module.chapter.vo.ProblemSummaryResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChapterMapper {

    @Select("""
            SELECT
                c.id,
                c.article_id,
                c.title,
                c.sort_order,
                c.estimated_minutes,
                c.status,
                c.created_at,
                c.updated_at,
                COUNT(p.id) AS problem_count
            FROM fs_chapter c
            LEFT JOIN fs_problem p ON p.chapter_id = c.id
                AND p.deleted = 0
                AND p.status = 'PUBLISHED'
            WHERE c.deleted = 0
              AND c.status = 'PUBLISHED'
              AND c.article_id = #{articleId}
            GROUP BY c.id, c.article_id, c.title, c.sort_order, c.estimated_minutes,
                     c.status, c.created_at, c.updated_at
            ORDER BY c.sort_order ASC, c.id ASC
            """)
    List<Chapter> findPublishedByArticleId(@Param("articleId") Long articleId);

    @Select("""
            SELECT
                id,
                article_id,
                title,
                content_md,
                sort_order,
                estimated_minutes,
                status,
                created_at,
                updated_at
            FROM fs_chapter
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND id = #{id}
            LIMIT 1
            """)
    Chapter findPublishedById(@Param("id") Long id);

    @Select("""
            SELECT id
            FROM fs_chapter
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND article_id = #{articleId}
              AND (sort_order < #{sortOrder} OR (sort_order = #{sortOrder} AND id < #{chapterId}))
            ORDER BY sort_order DESC, id DESC
            LIMIT 1
            """)
    Long findPrevChapterId(
            @Param("articleId") Long articleId,
            @Param("sortOrder") Integer sortOrder,
            @Param("chapterId") Long chapterId);

    @Select("""
            SELECT id
            FROM fs_chapter
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND article_id = #{articleId}
              AND (sort_order > #{sortOrder} OR (sort_order = #{sortOrder} AND id > #{chapterId}))
            ORDER BY sort_order ASC, id ASC
            LIMIT 1
            """)
    Long findNextChapterId(
            @Param("articleId") Long articleId,
            @Param("sortOrder") Integer sortOrder,
            @Param("chapterId") Long chapterId);

    @Select("""
            SELECT id
            FROM fs_problem
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND chapter_id = #{chapterId}
            ORDER BY sort_order ASC, id ASC
            """)
    List<Long> findPublishedProblemIds(@Param("chapterId") Long chapterId);

    @Select("""
            SELECT id, title, difficulty
            FROM fs_problem
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND chapter_id = #{chapterId}
            ORDER BY sort_order ASC, id ASC
            """)
    List<ProblemSummaryResponse> findPublishedProblems(@Param("chapterId") Long chapterId);
}
