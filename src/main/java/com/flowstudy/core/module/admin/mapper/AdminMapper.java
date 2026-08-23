package com.flowstudy.core.module.admin.mapper;

import com.flowstudy.core.module.admin.dto.AdminCouponRequest;
import com.flowstudy.core.module.admin.dto.AdminProductRequest;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminMapper {

    @Select("SELECT COUNT(*) totalUsers, SUM(status = 1) activeUsers, SUM(role = 'ADMIN') adminUsers FROM sys_user WHERE deleted = 0")
    Map<String, Object> dashboardUsers();

    @Select("SELECT COUNT(*) total, SUM(status = 'PUBLISHED') published, SUM(status = 'DRAFT') drafts FROM fs_tutorial WHERE deleted = 0")
    Map<String, Object> dashboardTutorials();

    @Select("SELECT COUNT(*) total, SUM(status = 'PUBLISHED') published, SUM(status = 'DRAFT') drafts FROM fs_blog WHERE deleted = 0")
    Map<String, Object> dashboardBlogs();

    @Select("SELECT COUNT(*) total, SUM(status = 'PUBLISHED') published, SUM(status = 'DRAFT') drafts FROM fs_problem WHERE deleted = 0")
    Map<String, Object> dashboardProblems();

    @Select("SELECT COUNT(*) total, SUM(status = 'PENDING') pending, SUM(status = 'PAID') paid FROM fs_membership_order")
    Map<String, Object> dashboardOrders();

    @Select("SELECT COUNT(*) FROM fs_blog WHERE deleted = 0 AND status = 'DRAFT'")
    long pendingBlogs();

    @Select("SELECT COUNT(*) FROM fs_tutorial WHERE deleted = 0 AND status = 'DRAFT'")
    long pendingTutorials();

    @Select("SELECT COUNT(*) FROM fs_problem WHERE deleted = 0 AND status = 'DRAFT'")
    long pendingProblems();

    @Select("""
            SELECT id, username, email, nickname, avatar_url, role, status, last_login_at, created_at
            FROM sys_user
            WHERE deleted = 0
              AND (#{keyword} IS NULL OR username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{role} IS NULL OR role = #{role})
              AND (#{status} IS NULL OR status = #{status})
            ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}
            """)
    List<Map<String, Object>> users(@Param("keyword") String keyword, @Param("role") String role,
                                    @Param("status") Integer status, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0 AND (#{keyword} IS NULL OR username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%')) AND (#{role} IS NULL OR role = #{role}) AND (#{status} IS NULL OR status = #{status})")
    long countUsers(@Param("keyword") String keyword, @Param("role") String role, @Param("status") Integer status);

    @Update("UPDATE sys_user SET status = #{status} WHERE id = #{id} AND deleted = 0")
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE sys_user SET role = #{role} WHERE id = #{id} AND deleted = 0")
    int updateUserRole(@Param("id") Long id, @Param("role") String role);

    @Select("SELECT id, title, summary, status, author_id, view_count, like_count, created_at, updated_at FROM fs_blog WHERE deleted = 0 AND (#{keyword} IS NULL OR title LIKE CONCAT('%', #{keyword}, '%')) AND (#{status} IS NULL OR status = #{status}) ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> blogs(@Param("keyword") String keyword, @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM fs_blog WHERE deleted = 0 AND (#{keyword} IS NULL OR title LIKE CONCAT('%', #{keyword}, '%')) AND (#{status} IS NULL OR status = #{status})")
    long countBlogs(@Param("keyword") String keyword, @Param("status") String status);

    @Update("UPDATE fs_blog SET status = #{status}, published_at = CASE WHEN #{status} = 'PUBLISHED' AND published_at IS NULL THEN NOW() ELSE published_at END WHERE id = #{id} AND deleted = 0")
    int updateBlogStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT id, title, summary, status, author_id, created_at, updated_at FROM fs_tutorial WHERE deleted = 0 AND (#{keyword} IS NULL OR title LIKE CONCAT('%', #{keyword}, '%')) AND (#{status} IS NULL OR status = #{status}) ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> tutorials(@Param("keyword") String keyword, @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM fs_tutorial WHERE deleted = 0 AND (#{keyword} IS NULL OR title LIKE CONCAT('%', #{keyword}, '%')) AND (#{status} IS NULL OR status = #{status})")
    long countTutorials(@Param("keyword") String keyword, @Param("status") String status);

    @Update("UPDATE fs_tutorial SET status = #{status}, published_at = CASE WHEN #{status} = 'PUBLISHED' AND published_at IS NULL THEN NOW() ELSE published_at END WHERE id = #{id} AND deleted = 0")
    int updateTutorialStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT id, title, difficulty, status, blog_id, submit_count, accepted_count, created_at, updated_at FROM fs_problem WHERE deleted = 0 AND (#{keyword} IS NULL OR title LIKE CONCAT('%', #{keyword}, '%')) AND (#{status} IS NULL OR status = #{status}) ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> problems(@Param("keyword") String keyword, @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM fs_problem WHERE deleted = 0 AND (#{keyword} IS NULL OR title LIKE CONCAT('%', #{keyword}, '%')) AND (#{status} IS NULL OR status = #{status})")
    long countProblems(@Param("keyword") String keyword, @Param("status") String status);

    @Update("UPDATE fs_problem SET status = #{status} WHERE id = #{id} AND deleted = 0")
    int updateProblemStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT id, name, description, price_cents, token_amount, stock, sold_count, sale_start_at, sale_end_at, status, sort_order FROM fs_membership_product ORDER BY id DESC")
    List<Map<String, Object>> products();

    @Insert("INSERT INTO fs_membership_product(name,description,price_cents,token_amount,stock,sale_start_at,sale_end_at,status,sort_order) VALUES(#{request.name},#{request.description},#{request.priceCents},#{request.tokenAmount},#{request.stock},#{request.saleStartAt},#{request.saleEndAt},COALESCE(#{request.status},'ACTIVE'),COALESCE(#{request.sortOrder},0))")
    int insertProduct(@Param("request") AdminProductRequest request);

    @Update("UPDATE fs_membership_product SET name=#{request.name},description=#{request.description},price_cents=#{request.priceCents},token_amount=#{request.tokenAmount},stock=#{request.stock},sale_start_at=#{request.saleStartAt},sale_end_at=#{request.saleEndAt},status=COALESCE(#{request.status},status),sort_order=COALESCE(#{request.sortOrder},sort_order) WHERE id=#{id}")
    int updateProduct(@Param("id") Long id, @Param("request") AdminProductRequest request);

    @Update("UPDATE fs_membership_product SET status=#{status} WHERE id=#{id}")
    int updateProductStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT id, name, coupon_type, discount_cents, min_order_cents, total_count, claimed_count, start_at, end_at, status FROM fs_coupon ORDER BY id DESC")
    List<Map<String, Object>> coupons();

    @Insert("INSERT INTO fs_coupon(name,coupon_type,discount_cents,min_order_cents,total_count,start_at,end_at,status) VALUES(#{request.name},#{request.couponType},#{request.discountCents},#{request.minOrderCents},#{request.totalCount},#{request.startAt},#{request.endAt},COALESCE(#{request.status},'ACTIVE'))")
    int insertCoupon(@Param("request") AdminCouponRequest request);

    @Update("UPDATE fs_coupon SET name=#{request.name},coupon_type=#{request.couponType},discount_cents=#{request.discountCents},min_order_cents=#{request.minOrderCents},total_count=#{request.totalCount},start_at=#{request.startAt},end_at=#{request.endAt},status=COALESCE(#{request.status},status) WHERE id=#{id}")
    int updateCoupon(@Param("id") Long id, @Param("request") AdminCouponRequest request);

    @Update("UPDATE fs_coupon SET status=#{status} WHERE id=#{id}")
    int updateCouponStatus(@Param("id") Long id, @Param("status") String status);

    @Insert("INSERT INTO fs_user_coupon(user_id,coupon_id,status,valid_until) SELECT #{userId},id,'AVAILABLE',end_at FROM fs_coupon WHERE id=#{couponId} AND status='ACTIVE' AND (end_at IS NULL OR end_at > NOW()) AND NOT EXISTS (SELECT 1 FROM fs_user_coupon WHERE user_id=#{userId} AND coupon_id=#{couponId})")
    int issueCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    @Select("SELECT o.id,o.order_no,o.user_id,u.username,o.product_id,p.name product_name,o.original_amount_cents,o.discount_amount_cents,o.payable_amount_cents,o.token_amount,o.status,o.created_at,o.paid_at FROM fs_membership_order o JOIN sys_user u ON u.id=o.user_id JOIN fs_membership_product p ON p.id=o.product_id WHERE (#{keyword} IS NULL OR o.order_no LIKE CONCAT('%',#{keyword},'%') OR u.username LIKE CONCAT('%',#{keyword},'%')) AND (#{status} IS NULL OR o.status=#{status}) ORDER BY o.id DESC LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> orders(@Param("keyword") String keyword, @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM fs_membership_order o JOIN sys_user u ON u.id=o.user_id WHERE (#{keyword} IS NULL OR o.order_no LIKE CONCAT('%',#{keyword},'%') OR u.username LIKE CONCAT('%',#{keyword},'%')) AND (#{status} IS NULL OR o.status=#{status})")
    long countOrders(@Param("keyword") String keyword, @Param("status") String status);

    @Insert("INSERT INTO fs_admin_audit_log(admin_id,module,action,target_type,target_id,request_summary,result,trace_id) VALUES(#{adminId},#{module},#{action},#{targetType},#{targetId},#{summary},#{result},#{traceId})")
    int insertAudit(@Param("adminId") Long adminId, @Param("module") String module, @Param("action") String action, @Param("targetType") String targetType, @Param("targetId") Long targetId, @Param("summary") String summary, @Param("result") String result, @Param("traceId") String traceId);

    @Select("SELECT id,admin_id,module,action,target_type,target_id,request_summary,result,trace_id,created_at FROM fs_admin_audit_log ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> auditLogs(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM fs_admin_audit_log")
    long countAuditLogs();
}
