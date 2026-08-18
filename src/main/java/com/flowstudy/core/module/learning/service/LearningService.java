package com.flowstudy.core.module.learning.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.trace.TraceContext;
import com.flowstudy.core.module.learning.client.AiAnalysisClient;
import com.flowstudy.core.module.learning.dto.LearningEventPayload;
import com.flowstudy.core.module.learning.dto.LearningEventRequest;
import com.flowstudy.core.module.learning.entity.LearningEvent;
import com.flowstudy.core.module.learning.mapper.LearningEventMapper;
import com.flowstudy.core.module.learning.mapper.LearningNoteMapper;
import com.flowstudy.core.module.learning.mapper.UserProfileMapper;
import com.flowstudy.core.module.learning.vo.LearningNoteResponse;
import com.flowstudy.core.module.learning.vo.ProfileAnalysisResponse;
import com.flowstudy.core.module.learning.vo.UserProfileResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningService {

    private static final int MAX_ANALYSIS_EVENTS = 200;

    private final LearningEventMapper eventMapper;
    private final UserProfileMapper profileMapper;
    private final LearningNoteMapper noteMapper;
    private final AiAnalysisClient aiAnalysisClient;
    private final ObjectMapper objectMapper;

    public LearningService(
            LearningEventMapper eventMapper,
            UserProfileMapper profileMapper,
            LearningNoteMapper noteMapper,
            AiAnalysisClient aiAnalysisClient,
            ObjectMapper objectMapper) {
        this.eventMapper = eventMapper;
        this.profileMapper = profileMapper;
        this.noteMapper = noteMapper;
        this.aiAnalysisClient = aiAnalysisClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordEvent(Long userId, LearningEventRequest request) {
        LearningEvent event = new LearningEvent();
        event.setUserId(userId);
        event.setEventType(request.eventType());
        event.setResourceType(request.resourceType());
        event.setResourceId(request.resourceId());
        event.setDurationSeconds(request.durationSeconds());
        event.setExtraJson(request.extraJson());
        event.setTraceId(TraceContext.getTraceId());
        event.setOccurredAt(request.occurredAt() == null ? LocalDateTime.now() : request.occurredAt());
        eventMapper.insert(event);
    }

    @Transactional
    public ProfileAnalysisResponse analyze(Long userId) {
        List<LearningEvent> events = eventMapper.findRecent(userId, MAX_ANALYSIS_EVENTS);
        List<LearningEventPayload> payloads = events.stream()
                .map(event -> new LearningEventPayload(
                        event.getEventType(),
                        event.getResourceType(),
                        event.getResourceId(),
                        event.getDurationSeconds(),
                        event.getExtraJson(),
                        event.getOccurredAt()))
                .toList();
        ProfileAnalysisResponse analysis;
        try {
            analysis = aiAnalysisClient.analyze(payloads);
        } catch (RuntimeException exception) {
            throw new BusinessException(50301, "AI learning analysis is temporarily unavailable",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        try {
            profileMapper.upsert(
                    userId,
                    objectMapper.writeValueAsString(analysis.ability()),
                    objectMapper.writeValueAsString(analysis.weakPoints()),
                    objectMapper.writeValueAsString(analysis.codingStyle()),
                    analysis.summaryMd());
        } catch (JsonProcessingException exception) {
            throw new BusinessException(50002, "learning profile serialization failed",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (analysis.learningSummaryMd() != null && !analysis.learningSummaryMd().isBlank()) {
            noteMapper.insert(userId, "AI 学习记录总结", analysis.learningSummaryMd());
        }
        return analysis;
    }

    public UserProfileResponse getProfile(Long userId) {
        return profileMapper.findByUserId(userId);
    }

    public List<LearningNoteResponse> getRecentNotes(Long userId) {
        return noteMapper.findRecent(userId, 20);
    }
}
