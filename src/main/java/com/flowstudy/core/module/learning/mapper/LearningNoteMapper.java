package com.flowstudy.core.module.learning.mapper;

import com.flowstudy.core.module.learning.vo.LearningNoteResponse;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LearningNoteMapper {

    @Insert("""
            INSERT INTO fs_learning_note (
                user_id, title, content_md, source, status
            ) VALUES (
                #{userId}, #{title}, #{contentMd}, 'AI', 'GENERATED'
            )
            """)
    int insert(@Param("userId") Long userId,
               @Param("title") String title,
               @Param("contentMd") String contentMd);

    @Select("""
            SELECT id, title, content_md, source, status, created_at
            FROM fs_learning_note
            WHERE user_id = #{userId} AND deleted = 0
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<LearningNoteResponse> findRecent(@Param("userId") Long userId, @Param("limit") int limit);
}
