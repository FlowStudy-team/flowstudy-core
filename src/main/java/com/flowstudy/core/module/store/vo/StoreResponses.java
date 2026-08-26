package com.flowstudy.core.module.store.vo;

import com.flowstudy.core.module.store.entity.MembershipOrder;
import com.flowstudy.core.module.store.entity.MembershipProduct;
import com.flowstudy.core.module.store.entity.UserCoupon;
import java.time.LocalDateTime;

public final class StoreResponses {
    private StoreResponses() {}
    public record Product(Long id, String name, String description, Integer priceCents, Integer tokenAmount,
                          Integer stock, Integer soldCount, LocalDateTime saleStartAt, LocalDateTime saleEndAt,
                          String status) {
        public static Product from(MembershipProduct p) { return new Product(p.getId(), p.getName(), p.getDescription(), p.getPriceCents(), p.getTokenAmount(), p.getStock(), p.getSoldCount(), p.getSaleStartAt(), p.getSaleEndAt(), p.getStatus()); }
    }
    public record Coupon(Long id, Long couponId, String name, String couponType, Integer discountCents,
                         Integer minOrderCents, String status, LocalDateTime validUntil) {
        public static Coupon from(UserCoupon c) { return new Coupon(c.getId(), c.getCouponId(), c.getName(), c.getCouponType(), c.getDiscountCents(), c.getMinOrderCents(), c.getStatus(), c.getValidUntil()); }
    }
    public record Order(Long id, String orderNo, Long productId, String productName, Integer originalAmountCents,
                        Integer discountAmountCents, Integer payableAmountCents, Integer tokenAmount, String status,
                        LocalDateTime createdAt, LocalDateTime paidAt) {
        public static Order from(MembershipOrder o) { return new Order(o.getId(), o.getOrderNo(), o.getProductId(), o.getProductName(), o.getOriginalAmountCents(), o.getDiscountAmountCents(), o.getPayableAmountCents(), o.getTokenAmount(), o.getStatus(), o.getCreatedAt(), o.getPaidAt()); }
        public static Order pending(String orderNo, MembershipProduct p) {
            return new Order(null, orderNo, p.getId(), p.getName(), p.getPriceCents(), 0, p.getPriceCents(), p.getTokenAmount(), "PENDING", null, null);
        }
    }
    public record TokenAccount(Integer totalTokens, Integer usedTokens, Integer availableTokens) {}
}
