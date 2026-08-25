package com.flowstudy.core.module.store.mq;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.module.store.entity.MembershipProduct;
import com.flowstudy.core.module.store.entity.UserCoupon;
import com.flowstudy.core.module.store.mapper.StoreMapper;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreOrderProcessor {
    private final StoreMapper mapper;

    public StoreOrderProcessor(StoreMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public void process(StoreOrderMessage message) {
        if (mapper.findOrderByNo(message.userId(), message.orderNo()) != null) {
            return;
        }
        MembershipProduct product = mapper.findProductForUpdate(message.productId());
        if (product == null || product.getSaleStartAt() != null && product.getSaleStartAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(46001, "product is not on sale", HttpStatus.BAD_REQUEST);
        }
        if (mapper.reserveProduct(product.getId()) != 1) {
            throw new BusinessException(46002, "product is sold out or sale ended", HttpStatus.CONFLICT);
        }
        UserCoupon coupon = message.userCouponId() == null
                ? null
                : mapper.findCouponForUpdate(message.userId(), message.userCouponId());
        if (message.userCouponId() != null && coupon == null) {
            throw new BusinessException(46003, "coupon is unavailable", HttpStatus.BAD_REQUEST);
        }
        int discount = coupon == null ? 0 : Math.min(coupon.getDiscountCents(), product.getPriceCents());
        if (coupon != null && product.getPriceCents() < coupon.getMinOrderCents()) {
            throw new BusinessException(46004, "order amount does not meet coupon requirement", HttpStatus.BAD_REQUEST);
        }
        if (coupon != null && mapper.useCoupon(message.userId(), coupon.getId()) != 1) {
            throw new BusinessException(46005, "coupon has already been used", HttpStatus.CONFLICT);
        }
        mapper.insertOrder(
                message.orderNo(), message.userId(), product.getId(), coupon == null ? null : coupon.getId(),
                product.getPriceCents(), discount, product.getPriceCents() - discount, product.getTokenAmount());
    }
}
