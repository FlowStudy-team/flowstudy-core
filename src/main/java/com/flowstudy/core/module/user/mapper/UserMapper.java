package com.flowstudy.core.module.user.mapper;

import com.flowstudy.core.module.user.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT id, username, email, password_hash, nickname, avatar_url, role, status, last_login_at
            FROM sys_user
            WHERE deleted = 0 AND username = #{username}
            LIMIT 1
            """)
    User findByUsername(@Param("username") String username);

    @Select("""
            SELECT id, username, email, password_hash, nickname, avatar_url, role, status, last_login_at
            FROM sys_user
            WHERE deleted = 0 AND email = #{email}
            LIMIT 1
            """)
    User findByEmail(@Param("email") String email);

    @Select("""
            SELECT id, username, email, password_hash, nickname, avatar_url, role, status, last_login_at
            FROM sys_user
            WHERE deleted = 0 AND id = #{id}
            LIMIT 1
            """)
    User findById(@Param("id") Long id);

    @Select("""
            SELECT id, username, email, password_hash, nickname, avatar_url, role, status, last_login_at
            FROM sys_user
            WHERE deleted = 0 AND (username = #{account} OR email = #{account})
            LIMIT 1
            """)
    User findByAccount(@Param("account") String account);

    @Insert("""
            INSERT INTO sys_user (username, email, password_hash, nickname, role, status, deleted)
            VALUES (#{username}, #{email}, #{passwordHash}, #{nickname}, 'USER', 1, 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE sys_user SET last_login_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateLastLoginAt(@Param("id") Long id);
}
