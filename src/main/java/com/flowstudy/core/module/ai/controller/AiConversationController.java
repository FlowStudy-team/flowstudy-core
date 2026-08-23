package com.flowstudy.core.module.ai.controller;

import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.ai.dto.AppendAiMessageRequest;
import com.flowstudy.core.module.ai.dto.CreateAiConversationRequest;
import com.flowstudy.core.module.ai.service.AiConversationService;
import com.flowstudy.core.module.ai.vo.AiConversationResponse;
import com.flowstudy.core.module.ai.vo.AiMessageResponse;
import com.flowstudy.core.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/conversations")
public class AiConversationController {

    private final AiConversationService service;

    public AiConversationController(AiConversationService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<AiConversationResponse>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return Result.success(service.list(user.id()));
    }

    @PostMapping
    public Result<AiConversationResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody CreateAiConversationRequest request) {
        return Result.success(service.create(user.id(), request));
    }

    @GetMapping("/{conversationId}/messages")
    public Result<List<AiMessageResponse>> messages(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long conversationId) {
        return Result.success(service.messages(user.id(), conversationId));
    }

    @PostMapping("/{conversationId}/messages")
    public Result<Void> append(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long conversationId,
            @Valid @RequestBody AppendAiMessageRequest request) {
        service.append(user.id(), conversationId, request);
        return Result.success(null);
    }
}
