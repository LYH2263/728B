package com.steam.mapper;

import com.steam.entity.RechargeOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RechargeOrderMapper {

    @Select("SELECT * FROM recharge_orders WHERE id = #{id}")
    RechargeOrder findById(Long id);

    @Select("SELECT * FROM recharge_orders WHERE order_no = #{orderNo}")
    RechargeOrder findByOrderNo(String orderNo);

    @Select("SELECT * FROM recharge_orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<RechargeOrder> findByUserId(Long userId);

    @Select("SELECT * FROM recharge_orders WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<RechargeOrder> findByUserIdPage(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM recharge_orders WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Select("SELECT * FROM recharge_orders WHERE status = #{status} ORDER BY created_at DESC")
    List<RechargeOrder> findByStatus(String status);

    @Insert("INSERT INTO recharge_orders (order_no, user_id, plan_id, plan_name, amount, bonus_amount, " +
            "total_amount, pay_method, status, expire_time, remark) " +
            "VALUES (#{orderNo}, #{userId}, #{planId}, #{planName}, #{amount}, #{bonusAmount}, " +
            "#{totalAmount}, #{payMethod}, #{status}, #{expireTime}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RechargeOrder order);

    @Update("UPDATE recharge_orders SET status = #{status}, pay_time = #{payTime}, " +
            "transaction_id = #{transactionId}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status,
                     @Param("payTime") java.time.LocalDateTime payTime,
                     @Param("transactionId") String transactionId);

    @Update("UPDATE recharge_orders SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status = 'PENDING'")
    int updateStatusIfPending(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT * FROM recharge_orders WHERE status = 'PENDING' AND expire_time < NOW()")
    List<RechargeOrder> findExpiredOrders();
}
