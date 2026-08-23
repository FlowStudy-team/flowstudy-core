package com.flowstudy.core.module.store.entity;

import java.time.LocalDateTime;

public class MembershipOrder {
    private Long id;
    private String orderNo;
    private Long productId;
    private String productName;
    private Integer originalAmountCents;
    private Integer discountAmountCents;
    private Integer payableAmountCents;
    private Integer tokenAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getOriginalAmountCents() { return originalAmountCents; }
    public void setOriginalAmountCents(Integer originalAmountCents) { this.originalAmountCents = originalAmountCents; }
    public Integer getDiscountAmountCents() { return discountAmountCents; }
    public void setDiscountAmountCents(Integer discountAmountCents) { this.discountAmountCents = discountAmountCents; }
    public Integer getPayableAmountCents() { return payableAmountCents; }
    public void setPayableAmountCents(Integer payableAmountCents) { this.payableAmountCents = payableAmountCents; }
    public Integer getTokenAmount() { return tokenAmount; }
    public void setTokenAmount(Integer tokenAmount) { this.tokenAmount = tokenAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
