package com.flowstudy.core.module.blog.mapper;

import com.flowstudy.core.module.blog.entity.Blog;
import com.flowstudy.core.module.blog.vo.ProblemSummaryResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BlogMapper {

    @Select("""
            SELECT
                b.id,
                b.tutorial_id,
                b.title,
                b.content_md,
                b.sort_order,
                b.estimated_minutes,
                b.status,
                b.created_at,
                b.updated_at,
                COUNT(p.id) AS problem_count
            FROM fs_blog b
            LEFT JOIN fs_problem p ON p.blog_id = b.id
                AND p.deleted = 0
                AND p.status = 'PUBLISHED'
            WHERE b.deleted = 0
              AND b.status = 'PUBLISHED'
              AND b.tutorial_id = #{tutorialId}
            GROUP BY b.id, b.tutorial_id, b.title, b.content_md, b.sort_order, b.estimated_minutes,
                     b.status, b.created_at, b.updated_at
            ORDER BY b.sort_order ASC, b.id ASC
            """)
    List<Blog> findPublishedByTutorialId(@Param("tutorialId") Long tutorialId);

    @Select("""
            SELECT COUNT(1)
            FROM fs_blog b
            WHERE b.deleted = 0
              AND b.status = 'PUBLISHED'
              AND (#{tutorialId} IS NULL OR b.tutorial_id = #{tutorialId})
              AND (#{standalone} = FALSE OR b.tutorial_id IS NULL)
              AND (#{keyword} IS NULL OR b.title LIKE CONCAT('%', #{keyword}, '%')
                   OR b.summary LIKE CONCAT('%', #{keyword}, '%')
                   OR b.content_md LIKE CONCAT('%', #{keyword}, '%'))
            """)
    long countPublished(
            @Param("tutorialId") Long tutorialId,
            @Param("standalone") boolean standalone,
            @Param("keyword") String keyword);

    @Select("""
            SELECT
                b.id,
                b.tutorial_id,
                b.title,
                b.content_md,
                b.sort_order,
                b.estimated_minutes,
                b.status,
                b.created_at,
                b.updated_at,
                COUNT(p.id) AS problem_count
            FROM fs_blog b
            LEFT JOIN fs_problem p ON p.blog_id = b.id
                AND p.deleted = 0
                AND p.status = 'PUBLISHED'
            WHERE b.deleted = 0
              AND b.status = 'PUBLISHED'
              AND (#{tutorialId} IS NULL OR b.tutorial_id = #{tutorialId})
              AND (#{standalone} = FALSE OR b.tutorial_id IS NULL)
              AND (#{keyword} IS NULL OR b.title LIKE CONCAT('%', #{keyword}, '%')
                   OR b.summary LIKE CONCAT('%', #{keyword}, '%')
                   OR b.content_md LIKE CONCAT('%', #{keyword}, '%'))
            GROUP BY b.id, b.tutorial_id, b.title, b.content_md, b.sort_order, b.estimated_minutes,
                     b.status, b.created_at, b.updated_at
            ORDER BY b.sort_order ASC, b.published_at DESC, b.created_at DESC, b.id ASC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<Blog> findPublishedPage(
            @Param("tutorialId") Long tutorialId,
            @Param("standalone") boolean standalone,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Select("""
            SELECT
                id,
                tutorial_id,
                title,
                content_md,
                sort_order,
                estimated_minutes,
                status,
                created_at,
                updated_at
            FROM fs_blog
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND id = #{id}
            LIMIT 1
            """)
    Blog findPublishedById(@Param("id") Long id);

    @Select("""
            SELECT id
            FROM fs_blog
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND tutorial_id = #{tutorialId}
              AND (sort_order < #{sortOrder} OR (sort_order = #{sortOrder} AND id < #{blogId}))
            ORDER BY sort_order DESC, id DESC
            LIMIT 1
            """)
    Long findPrevBlogId(
            @Param("tutorialId") Long tutorialId,
            @Param("sortOrder") Integer sortOrder,
            @Param("blogId") Long blogId);

    @Select("""
            SELECT id
            FROM fs_blog
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND tutorial_id = #{tutorialId}
              AND (sort_order > #{sortOrder} OR (sort_order = #{sortOrder} AND id > #{blogId}))
            ORDER BY sort_order ASC, id ASC
            LIMIT 1
            """)
    Long findNextBlogId(
            @Param("tutorialId") Long tutorialId,
            @Param("sortOrder") Integer sortOrder,
            @Param("blogId") Long blogId);

    @Select("""
            SELECT id
            FROM fs_problem
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND blog_id = #{blogId}
            ORDER BY sort_order ASC, id ASC
            """)
    List<Long> findPublishedProblemIds(@Param("blogId") Long blogId);

    @Select("""
            SELECT id, title, difficulty
            FROM fs_problem
            WHERE deleted = 0
              AND status = 'PUBLISHED'
              AND blog_id = #{blogId}
            ORDER BY sort_order ASC, id ASC
            """)
    List<ProblemSummaryResponse> findPublishedProblems(@Param("blogId") Long blogId);

    @Select("""
            SELECT id, tutorial_id, author_id, title, content_md, summary,
                   sort_order, estimated_minutes, status, created_at, updated_at
            FROM fs_blog
            WHERE deleted = 0 AND id = #{id}
            LIMIT 1
            """)
    Blog findById(@Param("id") Long id);

    @org.apache.ibatis.annotations.Insert("""
            INSERT INTO fs_blog (tutorial_id, author_id, title, content_md, summary,
                                 sort_order, estimated_minutes, status, published_at)
            VALUES (#{tutorialId}, #{authorId}, #{title}, #{contentMd}, #{summary},
                    COALESCE(#{sortOrder}, 0), #{estimatedMinutes}, #{status},
                    CASE WHEN #{status} = 'PUBLISHED' THEN NOW() ELSE NULL END)
            """)
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Blog blog);

    @org.apache.ibatis.annotations.Update("""
            UPDATE fs_blog
            SET title = #{title},
                content_md = #{contentMd},
                summary = #{summary},
                tutorial_id = #{tutorialId},
                sort_order = COALESCE(#{sortOrder}, 0),
                estimated_minutes = #{estimatedMinutes},
                status = #{status},
                published_at = CASE WHEN #{status} = 'PUBLISHED' AND published_at IS NULL
                                    THEN NOW() ELSE published_at END,
                updated_at = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int update(Blog blog);
}
