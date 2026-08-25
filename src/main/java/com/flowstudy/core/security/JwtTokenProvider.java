package com.flowstudy.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessExpireSeconds;
    private final long refreshExpireSeconds;

    public JwtTokenProvider(
            @Value("${auth.jwt.private-key:}") String privateKeyPem,
            @Value("${auth.jwt.public-key:}") String publicKeyPem,
            @Value("${auth.jwt.access-expire-seconds:900}") long accessExpireSeconds,
            @Value("${auth.jwt.refresh-expire-seconds:2592000}") long refreshExpireSeconds,
            @Value("${auth.jwt.allow-ephemeral:false}") boolean allowEphemeral) {
        try {
            KeyPair pair = privateKeyPem.isBlank() || publicKeyPem.isBlank()
                    ? (allowEphemeral ? generateEphemeralKeyPair() : null)
                    : loadKeyPair(privateKeyPem, publicKeyPem);
            if (pair == null) {
                throw new IllegalStateException("JWT RSA keys are not configured");
            }
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load JWT RSA keys", exception);
        }
        this.accessExpireSeconds = accessExpireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    public String createAccessToken(AuthenticatedUser user) {
        return createToken(user, "access", null, accessExpireSeconds);
    }

    public String createRefreshToken(AuthenticatedUser user, String deviceId) {
        return createToken(user, "refresh", deviceId, refreshExpireSeconds);
    }

    private String createToken(AuthenticatedUser user, String tokenType, String deviceId, long expireSeconds) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(user.id().toString())
                .claim("username", user.username())
                .claim("role", user.role())
                .claim("token_type", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .id(UUID.randomUUID().toString());
        if (deviceId != null) {
            builder.claim("device_id", deviceId);
        }
        return builder.signWith(privateKey, Jwts.SIG.RS256).compact();
    }

    public AuthenticatedUser parseAccessToken(String token) {
        Claims claims = parse(token, "access");
        return new AuthenticatedUser(
                Long.valueOf(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("role", String.class));
    }

    public RefreshClaims parseRefreshToken(String token) {
        Claims claims = parse(token, "refresh");
        return new RefreshClaims(
                Long.valueOf(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("role", String.class),
                claims.get("device_id", String.class),
                claims.getId());
    }

    private Claims parse(String token, String expectedType) {
        Claims claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
        if (!expectedType.equals(claims.get("token_type", String.class))) {
            throw new JwtException("unexpected token type");
        }
        return claims;
    }

    public long getAccessExpireSeconds() {
        return accessExpireSeconds;
    }

    public long getRefreshExpireSeconds() {
        return refreshExpireSeconds;
    }

    private static KeyPair loadKeyPair(String privatePem, String publicPem) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(decodePem(privatePem, "PRIVATE KEY")));
        PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(decodePem(publicPem, "PUBLIC KEY")));
        return new KeyPair(publicKey, privateKey);
    }

    private static byte[] decodePem(String pem, String type) {
        String value = pem.trim();
        if (!value.contains("-----BEGIN")) {
            String decoded = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
            if (decoded.contains("-----BEGIN")) return decodePem(decoded, type);
        }
        String normalized = value.replace("\\n", "\n").replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "").replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    private static KeyPair generateEphemeralKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public record RefreshClaims(Long userId, String username, String role, String deviceId, String jti) {
    }
}
