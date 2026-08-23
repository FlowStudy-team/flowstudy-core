package com.flowstudy.core.module.ai.mapper;

import com.flowstudy.core.module.ai.entity.AiConversation;
import com.flowstudy.core.module.ai.vo.AiConversationResponse;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiConversationMapper {

    @Insert("""
            INSERT INTO fs_ai_conversation (user_id, tutorial_id, blog_id, problem_id, title)
            VALUES (#{userId}, #{tutorialId}, #{blogId}, #{problemId}, #{title})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiConversation conversation);

    @Select("""
            SELECT id, title, status, tutorial_id, blog_id, problem_id, created_at, updated_at
            FROM fs_ai_conversation
            WHERE user_id = #{userId} AND deleted = 0
            ORDER BY updated_at DESC, id DESC
            """)
    List<AiConversationResponse> findByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT id, title, status, tutorial_id, blog_id, problem_id, created_at, updated_at
            FROM fs_ai_conversation
            WHERE id = #{conversationId} AND user_id = #{userId} AND deleted = 0
            LIMIT 1
            """)
    AiConversationResponse findOwned(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId);
}
