package com.flowstudy.core.module.content.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Contract adapter for the optional flowstudy-ai content summarization endpoint. */
@Component
public class ContentSummaryClient {
    private final RestClient restClient;
    private final String internalToken;
    private final boolean enabled;

    public ContentSummaryClient(
            @Value("${flowstudy.ai.base-url:http://localhost:8000}") String baseUrl,
            @Value("${flowstudy.ai.internal-token:}") String internalToken,
            @Value("${flowstudy.ai.content-summary-enabled:false}") boolean enabled) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.internalToken = internalToken;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Summary summarize(String title, String content) {
        return restClient.post()
                .uri("/api/v1/ai/content/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Internal-Token", internalToken)
                .body(new SummaryRequest(title, content))
                .retrieve()
                .body(Summary.class);
    }

    private record SummaryRequest(String title, String content) { }

    public record Summary(
            String summary,
            @JsonProperty("keywords") List<String> keywords) { }
}
