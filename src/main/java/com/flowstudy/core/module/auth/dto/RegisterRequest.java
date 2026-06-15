package com.flowstudy.core.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 64, message = "username length must be between 3 and 64")
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "username may only contain letters, numbers, and underscores")
        String username,
        @Email(message = "email format is invalid")
        @Size(max = 128, message = "email is too long")
        String email,
        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password length must be between 8 and 72")
        String password,
        @Size(max = 64, message = "nickname is too long")
        String nickname) {
}
