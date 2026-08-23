package com.flowstudy.core.module.store.entity;

import java.time.LocalDateTime;

public class UserCoupon {
    private Long id;
    private Long couponId;
    private String name;
    private String couponType;
    private Integer discountCents;
    private Integer minOrderCents;
    private String status;
    private LocalDateTime validUntil;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCouponType() { return couponType; }
    public void setCouponType(String couponType) { this.couponType = couponType; }
    public Integer getDiscountCents() { return discountCents; }
    public void setDiscountCents(Integer discountCents) { this.discountCents = discountCents; }
    public Integer getMinOrderCents() { return minOrderCents; }
    public void setMinOrderCents(Integer minOrderCents) { this.minOrderCents = minOrderCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
}
