package com.flowstudy.core.module.auth.vo;

public record AuthTokenResult(LoginResponse loginResponse, String refreshToken) {
}
