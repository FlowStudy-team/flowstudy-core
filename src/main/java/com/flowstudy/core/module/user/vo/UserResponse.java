package com.flowstudy.core.module.user.vo;

import com.flowstudy.core.module.user.entity.User;

public record UserResponse(
        Long id,
        String username,
        String email,
        String nickname,
        String avatarUrl,
        String role) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getNickname(), user.getAvatarUrl(), user.getRole());
    }
}
