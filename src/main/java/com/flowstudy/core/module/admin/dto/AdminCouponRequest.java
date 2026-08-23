package com.flowstudy.core.module.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record AdminCouponRequest(
        @NotBlank String name,
        @NotBlank String couponType,
        @Min(0) Integer discountCents,
        @Min(0) Integer minOrderCents,
        @Min(0) Integer totalCount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String status) {
}
