package com.flowstudy.core.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionService {

    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('HGET', KEYS[1], 'refreshTokenHash'); "
                    + "if not current or current ~= ARGV[1] then return 0 end; "
                    + "redis.call('HSET', KEYS[1], 'refreshTokenHash', ARGV[2], 'refreshTokenJti', ARGV[3], 'status', 'ACTIVE'); "
                    + "redis.call('EXPIRE', KEYS[1], ARGV[4]); return 1;",
            Long.class);

    private final StringRedisTemplate redis;

    public AuthSessionService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void create(Long userId, String deviceId, String refreshToken, String jti, long expireSeconds) {
        String key = key(userId, deviceId);
        redis.opsForHash().put(key, "refreshTokenHash", hash(refreshToken));
        redis.opsForHash().put(key, "refreshTokenJti", jti);
        redis.opsForHash().put(key, "status", "ACTIVE");
        redis.opsForHash().put(key, "userId", userId.toString());
        redis.opsForHash().put(key, "deviceId", deviceId);
        redis.expire(key, Duration.ofSeconds(expireSeconds));
    }

    public boolean rotate(Long userId, String deviceId, String oldToken, String newToken, String newJti,
                          long expireSeconds) {
        Long result = redis.execute(ROTATE_SCRIPT, java.util.List.of(key(userId, deviceId)),
                hash(oldToken), hash(newToken), newJti, Long.toString(expireSeconds));
        return Long.valueOf(1L).equals(result);
    }

    public void revoke(Long userId, String deviceId) {
        redis.delete(key(userId, deviceId));
    }

    private String key(Long userId, String deviceId) {
        return "auth:session:" + userId + ":" + deviceId;
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash refresh token", exception);
        }
    }
}
