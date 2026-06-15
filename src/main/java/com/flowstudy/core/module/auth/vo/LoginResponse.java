package com.flowstudy.core.module.auth.vo;

import com.flowstudy.core.module.user.vo.UserResponse;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {
}
