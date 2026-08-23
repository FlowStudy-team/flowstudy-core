package com.flowstudy.core.module.ai.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.module.ai.dto.AppendAiMessageRequest;
import com.flowstudy.core.module.ai.dto.CreateAiConversationRequest;
import com.flowstudy.core.module.ai.entity.AiConversation;
import com.flowstudy.core.module.ai.mapper.AiConversationMapper;
import com.flowstudy.core.module.ai.mapper.AiMessageMapper;
import com.flowstudy.core.module.ai.vo.AiConversationResponse;
import com.flowstudy.core.module.ai.vo.AiMessageResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AiConversationService {

    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;

    public AiConversationService(AiConversationMapper conversationMapper, AiMessageMapper messageMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    public List<AiConversationResponse> list(Long userId) {
        return conversationMapper.findByUserId(userId);
    }

    public AiConversationResponse create(Long userId, CreateAiConversationRequest request) {
        var conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTutorialId(request.tutorialId());
        conversation.setBlogId(request.blogId());
        conversation.setProblemId(request.problemId());
        conversation.setTitle(request.title());
        conversationMapper.insert(conversation);
        return conversationMapper.findOwned(userId, conversation.getId());
    }

    public List<AiMessageResponse> messages(Long userId, Long conversationId) {
        requireOwned(userId, conversationId);
        return messageMapper.findByConversation(userId, conversationId);
    }

    public void append(Long userId, Long conversationId, AppendAiMessageRequest request) {
        requireOwned(userId, conversationId);
        messageMapper.insert(conversationId, userId, request.role(), request.content(), request.modelName(), request.traceId());
    }

    private AiConversationResponse requireOwned(Long userId, Long conversationId) {
        var conversation = conversationMapper.findOwned(userId, conversationId);
        if (conversation == null) {
            throw new BusinessException(40401, "AI conversation not found", HttpStatus.NOT_FOUND);
        }
        return conversation;
    }
}
