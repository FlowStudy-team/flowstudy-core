package com.flowstudy.core.module.admin.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.common.trace.TraceContext;
import com.flowstudy.core.module.admin.dto.AdminCouponRequest;
import com.flowstudy.core.module.admin.dto.AdminProductRequest;
import com.flowstudy.core.module.admin.mapper.AdminMapper;
import com.flowstudy.core.module.content.ContentStatusMachine;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final AdminMapper mapper;

    public AdminService(AdminMapper mapper) { this.mapper = mapper; }

    public Map<String, Object> dashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("users", mapper.dashboardUsers());
        result.put("tutorials", mapper.dashboardTutorials());
        result.put("blogs", mapper.dashboardBlogs());
        result.put("problems", mapper.dashboardProblems());
        result.put("orders", mapper.dashboardOrders());
        result.put("pendingContent", Map.of("blogs", mapper.pendingBlogs(), "tutorials", mapper.pendingTutorials(), "problems", mapper.pendingProblems()));
        return result;
    }

    public PageResponse<Map<String, Object>> users(String keyword, String role, Integer status, int page, int size) {
        return page(mapper.users(keyword, role, status, size, offset(page, size)), mapper.countUsers(keyword, role, status), page, size);
    }
    public void userStatus(Long operatorId, Long id, Integer status) {
        if (!List.of(0, 1).contains(status)) throw bad("status must be 0 or 1");
        if (operatorId.equals(id) && status == 0) throw bad("cannot disable current admin");
        require(mapper.updateUserStatus(id, status)); audit(operatorId, "USER", "UPDATE_STATUS", "USER", id, "status=" + status);
    }
    public void userRole(Long operatorId, Long id, String role) {
        if (!List.of("USER", "ADMIN").contains(role)) throw bad("role must be USER or ADMIN");
        if (operatorId.equals(id) && "USER".equals(role)) throw bad("cannot demote current admin");
        require(mapper.updateUserRole(id, role)); audit(operatorId, "USER", "UPDATE_ROLE", "USER", id, "role=" + role);
    }

    public PageResponse<Map<String, Object>> content(String type, String keyword, String status, int page, int size) {
        List<Map<String, Object>> records;
        long total;
        if ("blogs".equals(type)) { records = mapper.blogs(keyword, status, size, offset(page, size)); total = mapper.countBlogs(keyword, status); }
        else if ("tutorials".equals(type)) { records = mapper.tutorials(keyword, status, size, offset(page, size)); total = mapper.countTutorials(keyword, status); }
        else if ("problems".equals(type)) { records = mapper.problems(keyword, status, size, offset(page, size)); total = mapper.countProblems(keyword, status); }
        else throw bad("unsupported content type");
        return page(records, total, page, size);
    }
    public void contentStatus(Long adminId, String type, Long id, String status) {
        String normalizedStatus = ContentStatusMachine.normalize(status, "DRAFT");
        int updated = switch (type) {
            case "blogs" -> mapper.updateBlogStatus(id, normalizedStatus);
            case "tutorials" -> mapper.updateTutorialStatus(id, normalizedStatus);
            case "problems" -> mapper.updateProblemStatus(id, normalizedStatus);
            default -> throw bad("unsupported content type");
        };
        require(updated); audit(adminId, "CONTENT", "UPDATE_STATUS", type, id, "status=" + normalizedStatus);
    }

    public List<Map<String, Object>> products() { return mapper.products(); }
    @Transactional public void createProduct(Long adminId, AdminProductRequest request) { mapper.insertProduct(request); audit(adminId, "PRODUCT", "CREATE", "PRODUCT", null, request.name()); }
    @Transactional public void updateProduct(Long adminId, Long id, AdminProductRequest request) { require(mapper.updateProduct(id, request)); audit(adminId, "PRODUCT", "UPDATE", "PRODUCT", id, request.name()); }
    public void productStatus(Long adminId, Long id, String status) { require(mapper.updateProductStatus(id, status)); audit(adminId, "PRODUCT", "UPDATE_STATUS", "PRODUCT", id, "status=" + status); }

    public List<Map<String, Object>> coupons() { return mapper.coupons(); }
    @Transactional public void createCoupon(Long adminId, AdminCouponRequest request) { mapper.insertCoupon(request); audit(adminId, "COUPON", "CREATE", "COUPON", null, request.name()); }
    @Transactional public void updateCoupon(Long adminId, Long id, AdminCouponRequest request) { require(mapper.updateCoupon(id, request)); audit(adminId, "COUPON", "UPDATE", "COUPON", id, request.name()); }
    public void couponStatus(Long adminId, Long id, String status) { require(mapper.updateCouponStatus(id, status)); audit(adminId, "COUPON", "UPDATE_STATUS", "COUPON", id, "status=" + status); }
    public void issueCoupon(Long adminId, Long userId, Long couponId) { require(mapper.issueCoupon(userId, couponId)); audit(adminId, "COUPON", "ISSUE", "USER", userId, "couponId=" + couponId); }

    public PageResponse<Map<String, Object>> orders(String keyword, String status, int page, int size) { return page(mapper.orders(keyword, status, size, offset(page, size)), mapper.countOrders(keyword, status), page, size); }
    public PageResponse<Map<String, Object>> auditLogs(int page, int size) { return page(mapper.auditLogs(size, offset(page, size)), mapper.countAuditLogs(), page, size); }

    private void audit(Long adminId, String module, String action, String targetType, Long targetId, String summary) { mapper.insertAudit(adminId, module, action, targetType, targetId, summary, "SUCCESS", TraceContext.getTraceId()); }
    private void require(int count) { if (count == 0) throw new BusinessException(40400, "resource not found", HttpStatus.NOT_FOUND); }
    private BusinessException bad(String message) { return new BusinessException(40000, message, HttpStatus.BAD_REQUEST); }
    private int offset(int page, int size) { return Math.max(page - 1, 0) * size; }
    private PageResponse<Map<String, Object>> page(List<Map<String, Object>> records, long total, int page, int size) { return new PageResponse<>(records, total, page, size); }
}
