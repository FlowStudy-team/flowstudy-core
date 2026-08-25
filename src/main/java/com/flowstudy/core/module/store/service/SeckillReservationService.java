package com.flowstudy.core.module.store.service;

import com.flowstudy.core.common.exception.BusinessException;
import java.util.Collections;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Redis-side reservation state for the membership stock hot path. */
@Service
public class SeckillReservationService {
    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            "local stock = tonumber(redis.call('GET', KEYS[1]) or '-1'); "
                    + "if stock < 0 then return -2; end; "
                    + "if redis.call('EXISTS', KEYS[2]) == 1 then return -1; end; "
                    + "if stock <= 0 then return 0; end; "
                    + "redis.call('DECR', KEYS[1]); "
                    + "redis.call('HSET', KEYS[2], 'status', 'RESERVED', 'productId', ARGV[1]); "
                    + "redis.call('EXPIRE', KEYS[2], ARGV[2]); "
                    + "return 1",
            Long.class);
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('HGET', KEYS[2], 'status') ~= 'RESERVED' then return 0; end; "
                    + "redis.call('INCR', KEYS[1]); "
                    + "redis.call('HSET', KEYS[2], 'status', 'RELEASED'); "
                    + "return 1",
            Long.class);
    private static final DefaultRedisScript<Long> COMPLETE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('HGET', KEYS[1], 'status') ~= 'RESERVED' then return 0; end; "
                    + "redis.call('HSET', KEYS[1], 'status', 'COMPLETED'); "
                    + "return 1",
            Long.class);

    private final StringRedisTemplate redis;

    public SeckillReservationService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void initializeStock(Long productId, int stock) {
        redis.opsForValue().setIfAbsent(stockKey(productId), String.valueOf(Math.max(stock, 0)));
    }

    public void reserve(Long productId, int stock, String orderNo) {
        initializeStock(productId, stock);
        Long result = redis.execute(
                RESERVE_SCRIPT,
                java.util.List.of(stockKey(productId), reservationKey(orderNo)),
                String.valueOf(productId), "86400");
        if (result == null || result == -2) {
            throw new BusinessException(46009, "seckill stock is not initialized", HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (result == -1) {
            throw new BusinessException(46010, "order reservation already exists", HttpStatus.CONFLICT);
        }
        if (result == 0) {
            throw new BusinessException(46002, "product is sold out", HttpStatus.CONFLICT);
        }
    }

    public void release(Long productId, String orderNo) {
        redis.execute(RELEASE_SCRIPT, java.util.List.of(stockKey(productId), reservationKey(orderNo)));
    }

    public void complete(String orderNo) {
        redis.execute(COMPLETE_SCRIPT, Collections.singletonList(reservationKey(orderNo)));
    }

    private String stockKey(Long productId) {
        return "flowstudy:seckill:product:stock:" + productId;
    }

    private String reservationKey(String orderNo) {
        return "flowstudy:seckill:reservation:" + orderNo;
    }
}
