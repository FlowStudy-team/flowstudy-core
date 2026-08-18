package com.flowstudy.core.module.store.controller;

import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.store.dto.CreateOrderRequest;
import com.flowstudy.core.module.store.service.StoreService;
import com.flowstudy.core.module.store.vo.StoreResponses;
import com.flowstudy.core.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/store")
public class StoreController {
    private final StoreService service;
    public StoreController(StoreService service) { this.service = service; }
    @GetMapping("/products") public Result<List<StoreResponses.Product>> products() { return Result.success(service.products()); }
    @GetMapping("/coupons") public Result<List<StoreResponses.Coupon>> coupons(@AuthenticationPrincipal AuthenticatedUser user) { return Result.success(service.coupons(user.id())); }
    @PostMapping("/coupons/{id}/claim") public Result<Void> claimCoupon(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) { service.claimCoupon(user.id(), id); return Result.success(null); }
    @PostMapping("/orders") public Result<StoreResponses.Order> create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateOrderRequest request) { return Result.success(service.createOrder(user.id(), request)); }
    @PostMapping("/orders/{id}/pay") public Result<StoreResponses.Order> pay(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) { return Result.success(service.pay(user.id(), id)); }
    @GetMapping("/orders") public Result<List<StoreResponses.Order>> orders(@AuthenticationPrincipal AuthenticatedUser user) { return Result.success(service.orders(user.id())); }
    @GetMapping("/account") public Result<StoreResponses.TokenAccount> account(@AuthenticationPrincipal AuthenticatedUser user) { return Result.success(service.account(user.id())); }
}
