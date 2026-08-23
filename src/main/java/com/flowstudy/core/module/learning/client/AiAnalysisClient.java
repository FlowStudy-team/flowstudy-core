package com.flowstudy.core.module.learning.client;

import com.flowstudy.core.module.learning.dto.LearningEventPayload;
import com.flowstudy.core.module.learning.vo.ProfileAnalysisResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiAnalysisClient {

    private final RestClient restClient;
    private final String internalToken;

    public AiAnalysisClient(
            @Value("${flowstudy.ai.base-url:http://localhost:8000}") String baseUrl,
            @Value("${flowstudy.ai.internal-token:}") String internalToken) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.internalToken = internalToken;
    }

    public ProfileAnalysisResponse analyze(List<LearningEventPayload> events) {
        return restClient.post()
                .uri("/api/v1/ai/profile/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Internal-Token", internalToken)
                .body(new EventsRequest(events))
                .retrieve()
                .body(ProfileAnalysisResponse.class);
    }

    private record EventsRequest(List<LearningEventPayload> events) { }
}
