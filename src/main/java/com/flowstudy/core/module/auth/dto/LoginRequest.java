package com.flowstudy.core.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "account is required")
        @Size(max = 128, message = "account is too long")
        String account,
        @NotBlank(message = "password is required")
        @Size(max = 72, message = "password is too long")
        String password) {
}
