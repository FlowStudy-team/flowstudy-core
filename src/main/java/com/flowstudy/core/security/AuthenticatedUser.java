package com.flowstudy.core.security;

public record AuthenticatedUser(Long id, String username, String role) {
}
