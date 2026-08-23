package com.flowstudy.core.common.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Redis-backed protection for endpoints that create expensive work. */
@Component
public class RedisRateLimitFilter extends OncePerRequestFilter {

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('incr', KEYS[1]); "
                    + "if current == 1 then redis.call('expire', KEYS[1], ARGV[1]); end; "
                    + "return current",
            Long.class);

    private final StringRedisTemplate redisTemplate;
    private final boolean enabled;
    private final int windowSeconds;
    private final int loginLimit;
    private final int codeSubmitLimit;
    private final int codeRunLimit;
    private final int storeOrderLimit;

    public RedisRateLimitFilter(
            StringRedisTemplate redisTemplate,
            @Value("${flowstudy.rate-limit.enabled:true}") boolean enabled,
            @Value("${flowstudy.rate-limit.window-seconds:60}") int windowSeconds,
            @Value("${flowstudy.rate-limit.login-per-window:10}") int loginLimit,
            @Value("${flowstudy.rate-limit.code-submit-per-window:10}") int codeSubmitLimit,
            @Value("${flowstudy.rate-limit.code-run-per-window:20}") int codeRunLimit,
            @Value("${flowstudy.rate-limit.store-order-per-window:5}") int storeOrderLimit) {
        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
        this.windowSeconds = Math.max(windowSeconds, 1);
        this.loginLimit = Math.max(loginLimit, 1);
        this.codeSubmitLimit = Math.max(codeSubmitLimit, 1);
        this.codeRunLimit = Math.max(codeRunLimit, 1);
        this.storeOrderLimit = Math.max(storeOrderLimit, 1);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || limitFor(request) == null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Limit limit = limitFor(request);
        String client = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        String key = "flowstudy:rate-limit:" + limit.name + ":" + client;
        try {
            Long count = redisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(windowSeconds));
            if (count != null && count > limit.maxRequests) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", String.valueOf(windowSeconds));
                response.getWriter().write("{\"code\":42900,\"message\":\"too many requests\"}");
                return;
            }
        } catch (DataAccessException ignored) {
            // A Redis outage must not make the core API unavailable.
        }
        filterChain.doFilter(request, response);
    }

    private Limit limitFor(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI();
        if (path.endsWith("/auth/login")) {
            return new Limit("login", loginLimit);
        }
        if (path.matches(".*/problems/[^/]+/submissions")) {
            return new Limit("code-submit", codeSubmitLimit);
        }
        if (path.matches(".*/problems/[^/]+/runs")) {
            return new Limit("code-run", codeRunLimit);
        }
        if (path.matches(".*/store/orders(?:/[^/]+/pay)?") || path.matches(".*/store/coupons/[^/]+/claim")) {
            return new Limit("store-order", storeOrderLimit);
        }
        return null;
    }

    private record Limit(String name, int maxRequests) { }
}
