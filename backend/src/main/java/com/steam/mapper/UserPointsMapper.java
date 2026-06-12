package com.steam.mapper;

import com.steam.entity.UserPoints;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserPointsMapper {

    @Select("SELECT * FROM points WHERE user_id = #{userId}")
    UserPoints findByUserId(Long userId);

    @Insert("INSERT INTO points (user_id, balance, total_earned, total_spent, consecutive_days) " +
            "VALUES (#{userId}, 0, 0, 0, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserPoints userPoints);

    @Update("UPDATE points SET balance = balance + #{amount}, total_earned = total_earned + #{amount}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int addPoints(@Param("userId") Long userId, @Param("amount") Integer amount);

    @Update("UPDATE points SET balance = balance - #{amount}, total_spent = total_spent + #{amount}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId} AND balance >= #{amount}")
    int deductPoints(@Param("userId") Long userId, @Param("amount") Integer amount);

    @Update("UPDATE points SET consecutive_days = #{consecutiveDays}, last_sign_date = #{lastSignDate}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int updateSignInfo(@Param("userId") Long userId,
                       @Param("consecutiveDays") Integer consecutiveDays,
                       @Param("lastSignDate") java.time.LocalDate lastSignDate);
}
