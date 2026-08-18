package com.flowstudy.core.module.store.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(@NotNull Long productId, Long userCouponId) {}
