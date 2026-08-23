package com.flowstudy.core.module.learning.mapper;

import com.flowstudy.core.module.learning.entity.LearningEvent;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.flowstudy.core.module.learning.vo.LearningOverviewResponse.DailyActivity;

@Mapper
public interface LearningEventMapper {

    @Insert("""
            INSERT INTO fs_behavior_event (
                user_id, event_type, resource_type, resource_id,
                duration_seconds, extra_json, trace_id, occurred_at
            ) VALUES (
                #{userId}, #{eventType}, #{resourceType}, #{resourceId},
                #{durationSeconds}, #{extraJson}, #{traceId}, #{occurredAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LearningEvent event);

    @Select("""
            SELECT id, user_id, event_type, resource_type, resource_id,
                   duration_seconds, extra_json, trace_id, occurred_at
            FROM fs_behavior_event
            WHERE user_id = #{userId}
            ORDER BY occurred_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<LearningEvent> findRecent(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            SELECT DATE(occurred_at) AS date, COUNT(1) AS count
            FROM fs_behavior_event
            WHERE user_id = #{userId}
              AND occurred_at >= #{from}
              AND occurred_at < #{to}
              AND event_type IN ('ARTICLE_READ', 'BLOG_READ', 'TUTORIAL_READ', 'DOCUMENT_READ', 'READ')
            GROUP BY DATE(occurred_at)
            ORDER BY date
            """)
    List<DailyActivity> findDailyReadingActivity(
            @Param("userId") Long userId,
            @Param("from") java.time.LocalDateTime from,
            @Param("to") java.time.LocalDateTime to);

    @Select("""
            SELECT COUNT(1)
            FROM fs_behavior_event
            WHERE user_id = #{userId}
              AND occurred_at >= #{from}
              AND occurred_at < #{to}
            """)
    long countEvents(@Param("userId") Long userId, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);

    @Select("""
            SELECT COUNT(DISTINCT DATE(occurred_at))
            FROM fs_behavior_event
            WHERE user_id = #{userId}
              AND occurred_at >= #{from}
              AND occurred_at < #{to}
            """)
    long countLearningDays(@Param("userId") Long userId, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
}
