package com.flowstudy.core.module.store.mq;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StoreOrderMessage(
        @JsonProperty("message_id") String messageId,
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("order_no") String orderNo,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("product_id") Long productId,
        @JsonProperty("user_coupon_id") Long userCouponId) {
}
