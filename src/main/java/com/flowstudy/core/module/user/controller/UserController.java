package com.flowstudy.core.module.user.controller;

import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.auth.service.AuthService;
import com.flowstudy.core.module.user.vo.UserResponse;
import com.flowstudy.core.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public Result<UserResponse> currentUser(@AuthenticationPrincipal AuthenticatedUser user) {
        return Result.success(authService.getCurrentUser(user.id()));
    }
}
