package com.flowstudy.core.module.admin.controller;

import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.admin.dto.AdminCouponRequest;
import com.flowstudy.core.module.admin.dto.AdminProductRequest;
import com.flowstudy.core.module.admin.service.AdminService;
import com.flowstudy.core.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }

    @GetMapping("/dashboard") public Result<Map<String, Object>> dashboard() { return Result.success(service.dashboard()); }
    @GetMapping("/users") public Result<PageResponse<Map<String, Object>>> users(@RequestParam(required=false) String keyword, @RequestParam(required=false) String role, @RequestParam(required=false) Integer status, @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) { return Result.success(service.users(keyword, role, status, page, size)); }
    @PutMapping("/users/{id}/status") public Result<Void> userStatus(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestParam Integer status) { service.userStatus(u.id(), id, status); return Result.success(null); }
    @PutMapping("/users/{id}/role") public Result<Void> userRole(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestParam String role) { service.userRole(u.id(), id, role); return Result.success(null); }

    @GetMapping("/{type}") public Result<PageResponse<Map<String,Object>>> content(@PathVariable String type, @RequestParam(required=false) String keyword, @RequestParam(required=false) String status, @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) { return Result.success(service.content(type, keyword, status, page, size)); }
    @PutMapping("/{type}/{id}/status") public Result<Void> contentStatus(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable String type, @PathVariable Long id, @RequestParam String status) { service.contentStatus(u.id(), type, id, status); return Result.success(null); }

    @GetMapping("/store/products") public Result<List<Map<String,Object>>> products() { return Result.success(service.products()); }
    @PostMapping("/store/products") public Result<Void> createProduct(@AuthenticationPrincipal AuthenticatedUser u, @Valid @RequestBody AdminProductRequest r) { service.createProduct(u.id(), r); return Result.success(null); }
    @PutMapping("/store/products/{id}") public Result<Void> updateProduct(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @Valid @RequestBody AdminProductRequest r) { service.updateProduct(u.id(), id, r); return Result.success(null); }
    @PutMapping("/store/products/{id}/status") public Result<Void> productStatus(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestParam String status) { service.productStatus(u.id(), id, status); return Result.success(null); }
    @GetMapping("/store/coupons") public Result<List<Map<String,Object>>> coupons() { return Result.success(service.coupons()); }
    @PostMapping("/store/coupons") public Result<Void> createCoupon(@AuthenticationPrincipal AuthenticatedUser u, @Valid @RequestBody AdminCouponRequest r) { service.createCoupon(u.id(), r); return Result.success(null); }
    @PutMapping("/store/coupons/{id}") public Result<Void> updateCoupon(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @Valid @RequestBody AdminCouponRequest r) { service.updateCoupon(u.id(), id, r); return Result.success(null); }
    @PutMapping("/store/coupons/{id}/status") public Result<Void> couponStatus(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestParam String status) { service.couponStatus(u.id(), id, status); return Result.success(null); }
    @PostMapping("/users/{userId}/coupons/{couponId}") public Result<Void> issueCoupon(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long userId, @PathVariable Long couponId) { service.issueCoupon(u.id(), userId, couponId); return Result.success(null); }

    @GetMapping("/orders") public Result<PageResponse<Map<String,Object>>> orders(@RequestParam(required=false) String keyword, @RequestParam(required=false) String status, @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) { return Result.success(service.orders(keyword, status, page, size)); }
    @GetMapping("/audit-logs") public Result<PageResponse<Map<String,Object>>> auditLogs(@RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) { return Result.success(service.auditLogs(page, size)); }
}
