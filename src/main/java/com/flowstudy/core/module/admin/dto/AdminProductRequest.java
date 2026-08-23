package com.flowstudy.core.module.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record AdminProductRequest(
        @NotBlank String name,
        String description,
        @Min(0) Integer priceCents,
        @Min(1) Integer tokenAmount,
        @Min(0) Integer stock,
        LocalDateTime saleStartAt,
        LocalDateTime saleEndAt,
        String status,
        Integer sortOrder) {
}
