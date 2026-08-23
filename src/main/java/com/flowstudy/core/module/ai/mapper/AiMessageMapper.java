package com.flowstudy.core.module.ai.mapper;

import com.flowstudy.core.module.ai.vo.AiMessageResponse;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiMessageMapper {

    @Insert("""
            INSERT INTO fs_ai_message (conversation_id, user_id, role, content, model_name, trace_id)
            VALUES (#{conversationId}, #{userId}, #{role}, #{content}, #{modelName}, #{traceId})
            """)
    int insert(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId,
            @Param("role") String role,
            @Param("content") String content,
            @Param("modelName") String modelName,
            @Param("traceId") String traceId);

    @Select("""
            SELECT id, conversation_id, role, content, model_name, trace_id, created_at
            FROM fs_ai_message
            WHERE conversation_id = #{conversationId} AND user_id = #{userId}
            ORDER BY id ASC
            """)
    List<AiMessageResponse> findByConversation(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId);
}
