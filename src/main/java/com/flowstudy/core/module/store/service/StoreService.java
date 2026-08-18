package com.flowstudy.core.module.store.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.module.store.dto.CreateOrderRequest;
import com.flowstudy.core.module.store.entity.MembershipProduct;
import com.flowstudy.core.module.store.entity.MembershipOrder;
import com.flowstudy.core.module.store.entity.UserCoupon;
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
    public StoreService(StoreMapper mapper) { this.mapper = mapper; }
    public List<StoreResponses.Product> products() { return mapper.findActiveProducts().stream().map(StoreResponses.Product::from).toList(); }
    public List<StoreResponses.Coupon> coupons(Long userId) { return mapper.findAvailableCoupons(userId).stream().map(StoreResponses.Coupon::from).toList(); }
    @Transactional
    public void claimCoupon(Long userId, Long couponId) {
        if (mapper.reserveCoupon(couponId) != 1 || mapper.grantCoupon(userId, couponId) != 1) {
            throw new BusinessException(46008, "coupon is unavailable or already claimed", HttpStatus.CONFLICT);
        }
    }
    @Transactional
    public StoreResponses.Order createOrder(Long userId, CreateOrderRequest request) {
        MembershipProduct product = mapper.findProductForUpdate(request.productId());
        if (product == null || product.getSaleStartAt() != null && product.getSaleStartAt().isAfter(LocalDateTime.now())) throw new BusinessException(46001, "product is not on sale", HttpStatus.BAD_REQUEST);
        if (mapper.reserveProduct(product.getId()) != 1) throw new BusinessException(46002, "product is sold out or sale ended", HttpStatus.CONFLICT);
        UserCoupon coupon = request.userCouponId() == null ? null : mapper.findCouponForUpdate(userId, request.userCouponId());
        if (request.userCouponId() != null && coupon == null) throw new BusinessException(46003, "coupon is unavailable", HttpStatus.BAD_REQUEST);
        int discount = coupon == null ? 0 : Math.min(coupon.getDiscountCents(), product.getPriceCents());
        if (coupon != null && product.getPriceCents() < coupon.getMinOrderCents()) throw new BusinessException(46004, "order amount does not meet coupon requirement", HttpStatus.BAD_REQUEST);
        if (coupon != null && mapper.useCoupon(userId, coupon.getId()) != 1) throw new BusinessException(46005, "coupon has already been used", HttpStatus.CONFLICT);
        String orderNo = "FS" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        mapper.insertOrder(orderNo, userId, product.getId(), coupon == null ? null : coupon.getId(), product.getPriceCents(), discount, product.getPriceCents() - discount, product.getTokenAmount());
        return StoreResponses.Order.from(mapper.findOrderByNo(userId, orderNo));
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
