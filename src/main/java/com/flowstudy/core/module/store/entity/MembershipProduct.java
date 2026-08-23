package com.flowstudy.core.module.store.entity;

import java.time.LocalDateTime;

public class MembershipProduct {
    private Long id;
    private String name;
    private String description;
    private Integer priceCents;
    private Integer tokenAmount;
    private Integer stock;
    private Integer soldCount;
    private LocalDateTime saleStartAt;
    private LocalDateTime saleEndAt;
    private String status;
    private Integer sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPriceCents() { return priceCents; }
    public void setPriceCents(Integer priceCents) { this.priceCents = priceCents; }
    public Integer getTokenAmount() { return tokenAmount; }
    public void setTokenAmount(Integer tokenAmount) { this.tokenAmount = tokenAmount; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getSoldCount() { return soldCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
    public LocalDateTime getSaleStartAt() { return saleStartAt; }
    public void setSaleStartAt(LocalDateTime saleStartAt) { this.saleStartAt = saleStartAt; }
    public LocalDateTime getSaleEndAt() { return saleEndAt; }
    public void setSaleEndAt(LocalDateTime saleEndAt) { this.saleEndAt = saleEndAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
