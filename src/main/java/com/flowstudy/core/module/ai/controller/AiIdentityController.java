package com.flowstudy.core.module.ai.controller;

import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.security.AuthenticatedUser;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Minimal authenticated identity contract for AI-owned, user-scoped memory. */
@RestController
@RequestMapping("/api/v1/ai/identity")
public class AiIdentityController {

    @GetMapping
    public Result<Map<String, Long>> current(@AuthenticationPrincipal AuthenticatedUser user) {
        return Result.success(Map.of("userId", user.id()));
    }
}
