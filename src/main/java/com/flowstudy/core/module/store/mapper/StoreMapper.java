package com.flowstudy.core.module.store.mapper;

import com.flowstudy.core.module.store.entity.MembershipOrder;
import com.flowstudy.core.module.store.entity.MembershipProduct;
import com.flowstudy.core.module.store.entity.UserCoupon;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StoreMapper {
    @Select("SELECT id,name,description,price_cents,token_amount,stock,sold_count,sale_start_at,sale_end_at,status,sort_order FROM fs_membership_product WHERE status='ACTIVE' AND (sale_start_at IS NULL OR sale_start_at <= NOW()) AND (sale_end_at IS NULL OR sale_end_at > NOW()) ORDER BY sort_order,id")
    List<MembershipProduct> findActiveProducts();

    @Select("SELECT id,name,description,price_cents,token_amount,stock,sold_count,sale_start_at,sale_end_at,status,sort_order FROM fs_membership_product WHERE id=#{id} AND status='ACTIVE' FOR UPDATE")
    MembershipProduct findProductForUpdate(@Param("id") Long id);

    @Update("UPDATE fs_membership_product SET stock=stock-1,sold_count=sold_count+1 WHERE id=#{id} AND stock > 0 AND status='ACTIVE' AND (sale_start_at IS NULL OR sale_start_at <= NOW()) AND (sale_end_at IS NULL OR sale_end_at > NOW())")
    int reserveProduct(@Param("id") Long id);

    @Select("SELECT uc.id,uc.coupon_id,c.name,c.coupon_type,c.discount_cents,c.min_order_cents,uc.status,uc.valid_until FROM fs_user_coupon uc JOIN fs_coupon c ON c.id=uc.coupon_id WHERE uc.user_id=#{userId} AND uc.status='AVAILABLE' AND uc.valid_until > NOW() ORDER BY uc.valid_until")
    List<UserCoupon> findAvailableCoupons(@Param("userId") Long userId);

    @Update("UPDATE fs_coupon SET claimed_count=claimed_count+1 WHERE id=#{couponId} AND status='ACTIVE' AND start_at <= NOW() AND end_at > NOW() AND (total_count=0 OR claimed_count < total_count)")
    int reserveCoupon(@Param("couponId") Long couponId);

    @Insert("INSERT INTO fs_user_coupon(user_id,coupon_id,status,valid_until) SELECT #{userId},id,'AVAILABLE',end_at FROM fs_coupon WHERE id=#{couponId}")
    int grantCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    @Select("SELECT uc.id,uc.coupon_id,c.name,c.coupon_type,c.discount_cents,c.min_order_cents,uc.status,uc.valid_until FROM fs_user_coupon uc JOIN fs_coupon c ON c.id=uc.coupon_id WHERE uc.id=#{id} AND uc.user_id=#{userId} AND uc.status='AVAILABLE' AND uc.valid_until > NOW() FOR UPDATE")
    UserCoupon findCouponForUpdate(@Param("userId") Long userId, @Param("id") Long id);

    @Update("UPDATE fs_user_coupon SET status='USED',used_at=NOW() WHERE id=#{id} AND user_id=#{userId} AND status='AVAILABLE'")
    int useCoupon(@Param("userId") Long userId, @Param("id") Long id);

    @Insert("INSERT INTO fs_membership_order(order_no,user_id,product_id,coupon_id,original_amount_cents,discount_amount_cents,payable_amount_cents,token_amount,status) VALUES(#{orderNo},#{userId},#{productId},#{couponId},#{original},#{discount},#{payable},#{tokens},'PENDING')")
    int insertOrder(@Param("orderNo") String orderNo, @Param("userId") Long userId, @Param("productId") Long productId, @Param("couponId") Long couponId, @Param("original") Integer original, @Param("discount") Integer discount, @Param("payable") Integer payable, @Param("tokens") Integer tokens);

    @Select("SELECT o.id,o.order_no,o.product_id,p.name product_name,o.original_amount_cents,o.discount_amount_cents,o.payable_amount_cents,o.token_amount,o.status,o.created_at,o.paid_at FROM fs_membership_order o JOIN fs_membership_product p ON p.id=o.product_id WHERE o.order_no=#{orderNo} AND o.user_id=#{userId}")
    MembershipOrder findOrderByNo(@Param("userId") Long userId, @Param("orderNo") String orderNo);

    @Select("SELECT o.id,o.order_no,o.product_id,p.name product_name,o.original_amount_cents,o.discount_amount_cents,o.payable_amount_cents,o.token_amount,o.status,o.created_at,o.paid_at FROM fs_membership_order o JOIN fs_membership_product p ON p.id=o.product_id WHERE o.id=#{id} AND o.user_id=#{userId}")
    MembershipOrder findOrder(@Param("userId") Long userId, @Param("id") Long id);

    @Select("SELECT o.id,o.order_no,o.product_id,p.name product_name,o.original_amount_cents,o.discount_amount_cents,o.payable_amount_cents,o.token_amount,o.status,o.created_at,o.paid_at FROM fs_membership_order o JOIN fs_membership_product p ON p.id=o.product_id WHERE o.user_id=#{userId} ORDER BY o.id DESC")
    List<MembershipOrder> findOrders(@Param("userId") Long userId);

    @Update("UPDATE fs_membership_order SET status='PAID',paid_at=NOW() WHERE id=#{id} AND user_id=#{userId} AND status='PENDING'")
    int markPaid(@Param("userId") Long userId, @Param("id") Long id);

    @Insert("INSERT INTO fs_token_account(user_id,total_tokens,used_tokens) VALUES(#{userId},#{tokens},0) ON DUPLICATE KEY UPDATE total_tokens=total_tokens+VALUES(total_tokens)")
    int grantTokens(@Param("userId") Long userId, @Param("tokens") Integer tokens);

    @Select("SELECT total_tokens,used_tokens,total_tokens-used_tokens available_tokens FROM fs_token_account WHERE user_id=#{userId}")
    StoreTokenRow findTokenAccount(@Param("userId") Long userId);

    record StoreTokenRow(Integer totalTokens, Integer usedTokens, Integer availableTokens) {}
}
