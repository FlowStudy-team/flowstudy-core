package com.flowstudy.core.module.store.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.module.store.dto.CreateOrderRequest;
import com.flowstudy.core.module.store.entity.MembershipProduct;
import com.flowstudy.core.module.store.entity.MembershipOrder;
import com.flowstudy.core.module.store.entity.UserCoupon;
import com.flowstudy.core.module.store.mq.StoreOrderPublisher;
import com.flowstudy.core.common.trace.TraceContext;
import com.flowstudy.core.module.store.mapper.StoreMapper;
import com.flowstudy.core.module.store.vo.StoreResponses;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {
    private final StoreMapper mapper;
    private final SeckillReservationService reservations;
    private final StoreOrderPublisher orderPublisher;
    public StoreService(StoreMapper mapper, SeckillReservationService reservations, StoreOrderPublisher orderPublisher) {
        this.mapper = mapper;
        this.reservations = reservations;
        this.orderPublisher = orderPublisher;
    }
    public List<StoreResponses.Product> products() {
        return mapper.findActiveProducts().stream().peek(p -> reservations.initializeStock(p.getId(), p.getStock()))
                .map(StoreResponses.Product::from).toList();
    }
    public List<StoreResponses.Coupon> coupons(Long userId) { return mapper.findAvailableCoupons(userId).stream().map(StoreResponses.Coupon::from).toList(); }
    @Transactional
    public void claimCoupon(Long userId, Long couponId) {
        if (mapper.reserveCoupon(couponId) != 1 || mapper.grantCoupon(userId, couponId) != 1) {
            throw new BusinessException(46008, "coupon is unavailable or already claimed", HttpStatus.CONFLICT);
        }
    }
    public StoreResponses.Order createOrder(Long userId, CreateOrderRequest request) {
        MembershipProduct product = mapper.findActiveProducts().stream()
                .filter(item -> item.getId().equals(request.productId())).findFirst().orElse(null);
        if (product == null || product.getSaleStartAt() != null && product.getSaleStartAt().isAfter(LocalDateTime.now())) throw new BusinessException(46001, "product is not on sale", HttpStatus.BAD_REQUEST);
        String orderNo = "FS" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        reservations.reserve(product.getId(), product.getStock(), orderNo);
        try {
            orderPublisher.publish(StoreOrderPublisher.message(
                    TraceContext.getTraceId(), orderNo, userId, product.getId(), request.userCouponId()));
            return StoreResponses.Order.pending(orderNo, product);
        } catch (RuntimeException exception) {
            reservations.release(product.getId(), orderNo);
            throw exception;
        }
    }
    @Transactional
    public StoreResponses.Order pay(Long userId, Long orderId) {
        MembershipOrder order = mapper.findOrder(userId, orderId);
        if (order == null) throw new BusinessException(46006, "order does not exist", HttpStatus.NOT_FOUND);
        if ("PAID".equals(order.getStatus())) return StoreResponses.Order.from(order);
        if (mapper.markPaid(userId, orderId) != 1) throw new BusinessException(46007, "order cannot be paid", HttpStatus.CONFLICT);
        mapper.grantTokens(userId, order.getTokenAmount());
        return StoreResponses.Order.from(mapper.findOrder(userId, orderId));
    }
    public List<StoreResponses.Order> orders(Long userId) { return mapper.findOrders(userId).stream().map(StoreResponses.Order::from).toList(); }
    public StoreResponses.TokenAccount account(Long userId) {
        StoreMapper.StoreTokenRow row = mapper.findTokenAccount(userId);
        return row == null ? new StoreResponses.TokenAccount(0, 0, 0) : new StoreResponses.TokenAccount(row.totalTokens(), row.usedTokens(), row.availableTokens());
    }
}
