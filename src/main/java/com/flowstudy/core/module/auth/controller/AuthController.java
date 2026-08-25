package com.flowstudy.core.module.auth.controller;

import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.auth.dto.LoginRequest;
import com.flowstudy.core.module.auth.dto.RegisterRequest;
import com.flowstudy.core.module.auth.service.AuthService;
import com.flowstudy.core.module.auth.vo.LoginResponse;
import com.flowstudy.core.module.auth.vo.AuthTokenResult;
import com.flowstudy.core.module.auth.vo.RegisterResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "flowstudy_refresh_token";

    private final AuthService authService;
    private final boolean secureCookie;
    private final String sameSite;
    private final long refreshExpireSeconds;

    public AuthController(AuthService authService,
                           @Value("${auth.cookie.secure:false}") boolean secureCookie,
                           @Value("${auth.cookie.same-site:Lax}") String sameSite,
                           @Value("${auth.jwt.refresh-expire-seconds:2592000}") long refreshExpireSeconds) {
        this.authService = authService;
        this.secureCookie = secureCookie;
        this.sameSite = sameSite;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       @RequestHeader("X-Device-Id") String deviceId,
                                       HttpServletResponse response) {
        AuthTokenResult result = authService.login(request, deviceId);
        writeRefreshCookie(response, result.refreshToken());
        return Result.success(result.loginResponse());
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestHeader("X-Device-Id") String deviceId,
                                         HttpServletRequest request, HttpServletResponse response) {
        AuthTokenResult result = authService.refresh(readRefreshCookie(request), deviceId);
        writeRefreshCookie(response, result.refreshToken());
        return Result.success(result.loginResponse());
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("X-Device-Id") String deviceId,
                               HttpServletRequest request, HttpServletResponse response) {
        authService.logout(readRefreshCookie(request), deviceId);
        clearRefreshCookie(response);
        return Result.success(null);
    }

    private String readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (REFRESH_COOKIE.equals(cookie.getName())) return cookie.getValue();
            }
        }
        throw new com.flowstudy.core.common.exception.BusinessException(40101, "refresh token is required",
                org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

    private void writeRefreshCookie(HttpServletResponse response, String value) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true).secure(secureCookie).sameSite(sameSite).path("/api/v1/auth")
                .maxAge(refreshExpireSeconds).build().toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true).secure(secureCookie).sameSite(sameSite).path("/api/v1/auth")
                .maxAge(0).build().toString());
    }
}
