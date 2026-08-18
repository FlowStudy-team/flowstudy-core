package com.flowstudy.core.module.learning.mapper;

import com.flowstudy.core.module.learning.entity.LearningEvent;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
