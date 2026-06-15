package com.flowstudy.core.module.auth.controller;

import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.auth.dto.LoginRequest;
import com.flowstudy.core.module.auth.dto.RegisterRequest;
import com.flowstudy.core.module.auth.service.AuthService;
import com.flowstudy.core.module.auth.vo.LoginResponse;
import com.flowstudy.core.module.auth.vo.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
}
