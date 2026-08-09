package com.flowstudy.core.module.tutorial.mapper;

import com.flowstudy.core.module.tutorial.entity.Tutorial;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TutorialMapper {

    @Select("""
            SELECT COUNT(1)
            FROM fs_tutorial a
            WHERE a.deleted = 0
              AND a.status = 'PUBLISHED'
              AND (#{keyword} IS NULL OR a.title LIKE CONCAT('%', #{keyword}, '%')
                   OR a.summary LIKE CONCAT('%', #{keyword}, '%'))
            """)
    long countPublished(@Param("keyword") String keyword);

    @Select("""
            SELECT
                a.id,
                a.title,
                a.summary,
                a.cover_url,
                a.author_id,
                COALESCE(u.nickname, u.username, 'FlowStudy') AS author_name,
                a.status,
                a.view_count,
                a.like_count,
                a.sort_order,
                a.published_at,
                a.created_at,
                a.updated_at,
                COUNT(DISTINCT c.id) AS blog_count,
                COUNT(DISTINCT p.id) AS problem_count
            FROM fs_tutorial a
            LEFT JOIN sys_user u ON u.id = a.author_id AND u.deleted = 0
            LEFT JOIN fs_blog c ON c.tutorial_id = a.id
                AND c.deleted = 0
                AND c.status = 'PUBLISHED'
            LEFT JOIN fs_problem p ON p.blog_id = c.id
                AND p.deleted = 0
                AND p.status = 'PUBLISHED'
            WHERE a.deleted = 0
              AND a.status = 'PUBLISHED'
              AND (#{keyword} IS NULL OR a.title LIKE CONCAT('%', #{keyword}, '%')
                   OR a.summary LIKE CONCAT('%', #{keyword}, '%'))
            GROUP BY a.id, a.title, a.summary, a.cover_url, a.author_id, u.nickname, u.username,
                     a.status, a.view_count, a.like_count, a.sort_order, a.published_at,
                     a.created_at, a.updated_at
            ORDER BY a.sort_order ASC, a.published_at DESC, a.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<Tutorial> findPublishedPage(
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Select("""
            SELECT
                a.id,
                a.title,
                a.summary,
                a.cover_url,
                a.author_id,
                COALESCE(u.nickname, u.username, 'FlowStudy') AS author_name,
                a.status,
                a.view_count,
                a.like_count,
                a.sort_order,
                a.published_at,
                a.created_at,
                a.updated_at,
                COUNT(DISTINCT c.id) AS blog_count,
                COUNT(DISTINCT p.id) AS problem_count
            FROM fs_tutorial a
            LEFT JOIN sys_user u ON u.id = a.author_id AND u.deleted = 0
            LEFT JOIN fs_blog c ON c.tutorial_id = a.id
                AND c.deleted = 0
                AND c.status = 'PUBLISHED'
            LEFT JOIN fs_problem p ON p.blog_id = c.id
                AND p.deleted = 0
                AND p.status = 'PUBLISHED'
            WHERE a.deleted = 0
              AND a.status = 'PUBLISHED'
              AND a.id = #{id}
            GROUP BY a.id, a.title, a.summary, a.cover_url, a.author_id, u.nickname, u.username,
                     a.status, a.view_count, a.like_count, a.sort_order, a.published_at,
                     a.created_at, a.updated_at
            LIMIT 1
            """)
    Tutorial findPublishedById(@Param("id") Long id);
}
