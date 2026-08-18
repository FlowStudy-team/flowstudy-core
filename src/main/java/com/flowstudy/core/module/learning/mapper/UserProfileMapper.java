package com.flowstudy.core.module.learning.mapper;

import com.flowstudy.core.module.learning.vo.UserProfileResponse;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserProfileMapper {

    @Insert("""
            INSERT INTO fs_user_profile (
                user_id, ability_json, weak_points_json, coding_style_json, summary_md
            ) VALUES (
                #{userId}, #{abilityJson}, #{weakPointsJson}, #{codingStyleJson}, #{summaryMd}
            ) ON DUPLICATE KEY UPDATE
                ability_json = VALUES(ability_json),
                weak_points_json = VALUES(weak_points_json),
                coding_style_json = VALUES(coding_style_json),
                summary_md = VALUES(summary_md)
            """)
    int upsert(
            @Param("userId") Long userId,
            @Param("abilityJson") String abilityJson,
            @Param("weakPointsJson") String weakPointsJson,
            @Param("codingStyleJson") String codingStyleJson,
            @Param("summaryMd") String summaryMd);

    @Select("""
            SELECT user_id, ability_json, weak_points_json, coding_style_json, summary_md, updated_at
            FROM fs_user_profile
            WHERE user_id = #{userId}
            LIMIT 1
            """)
    UserProfileResponse findByUserId(@Param("userId") Long userId);
}
