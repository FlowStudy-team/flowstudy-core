package com.flowstudy.core.module.content.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowstudy.core.module.blog.entity.Blog;
import com.flowstudy.core.module.content.vo.ContentSearchResponse;
import com.flowstudy.core.module.content.vo.ContentSearchResult;
import com.flowstudy.core.module.tutorial.entity.Tutorial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Optional Elasticsearch adapter. MySQL remains the local fallback backend. */
@Component
public class ContentSearchClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String index;

    public ContentSearchClient(
            ObjectMapper objectMapper,
            @Value("${flowstudy.search.enabled:false}") boolean enabled,
            @Value("${flowstudy.search.base-url:http://localhost:9200}") String baseUrl,
            @Value("${flowstudy.search.index:flowstudy-content}") String index) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.index = index;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ContentSearchResponse search(String keyword, int size, String searchAfter) {
        if (!enabled) {
            throw new IllegalStateException("elasticsearch search is disabled");
        }
        Map<String, Object> multiMatch = Map.of(
                "multi_match", Map.of(
                        "query", keyword,
                        "fields", List.of("title^6", "summary^3", "content^1", "tags^2"),
                        "type", "best_fields"));
        Map<String, Object> body = new HashMap<>();
        body.put("size", size);
        body.put("track_total_hits", true);
        body.put("query", Map.of("bool", Map.of(
                "must", List.of(multiMatch),
                "filter", List.of(Map.of("term", Map.of("status", "PUBLISHED"))))));
        body.put("sort", List.of(
                Map.of("_score", "desc"),
                Map.of("publishedAt", Map.of("order", "desc", "missing", "_last")),
                Map.of("_id", "desc")));
        if (searchAfter != null && !searchAfter.isBlank()) {
            try {
                body.put("search_after", objectMapper.readValue(searchAfter, List.class));
            } catch (Exception exception) {
                throw new IllegalArgumentException("searchAfter must be a JSON array", exception);
            }
        }
        JsonNode response = restClient.post()
                .uri("/" + index + "/_search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        return parseResponse(response);
    }

    public void indexBlog(Blog blog) {
        if (!enabled) return;
        Map<String, Object> document = Map.of(
                "type", "BLOG", "id", blog.getId(), "title", blog.getTitle(),
                "summary", blog.getSummary() == null ? "" : blog.getSummary(),
                "content", blog.getContentMd() == null ? "" : blog.getContentMd(),
                "status", blog.getStatus(), "publishedAt", blog.getPublishedAt() == null ? "" : blog.getPublishedAt());
        restClient.put().uri("/" + index + "/_doc/blog-" + blog.getId())
                .contentType(MediaType.APPLICATION_JSON).body(document).retrieve().toBodilessEntity();
    }

    public void deleteBlog(Long id) {
        if (!enabled) return;
        restClient.delete().uri("/" + index + "/_doc/blog-" + id).retrieve().toBodilessEntity();
    }

    private ContentSearchResponse parseResponse(JsonNode response) {
        JsonNode hits = response == null ? null : response.path("hits");
        List<ContentSearchResult> records = new ArrayList<>();
        String next = null;
        if (hits != null && hits.has("hits")) {
            for (JsonNode hit : hits.get("hits")) {
                JsonNode source = hit.path("_source");
                records.add(new ContentSearchResult(
                        source.path("type").asText("BLOG"), source.path("id").asLong(),
                        source.path("title").asText(), source.path("summary").asText(),
                        "/" + source.path("type").asText("BLOG").toLowerCase() + "s/" + source.path("id").asLong(),
                        hit.path("_score").asDouble(0)));
                if (hit.has("sort")) next = hit.get("sort").toString();
            }
        }
        long total = hits == null ? 0 : hits.path("total").isObject()
                ? hits.path("total").path("value").asLong() : hits.path("total").asLong();
        return new ContentSearchResponse(records, total, next, "elasticsearch");
    }
}
