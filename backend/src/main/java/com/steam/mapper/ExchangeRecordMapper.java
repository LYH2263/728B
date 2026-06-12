package com.steam.mapper;

import com.steam.entity.ExchangeRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExchangeRecordMapper {

    @Select("SELECT * FROM exchange_records WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<ExchangeRecord> findByUserId(@Param("userId") Long userId,
                                      @Param("offset") Integer offset,
                                      @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM exchange_records WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Insert("INSERT INTO exchange_records (user_id, product_id, product_name, product_image, " +
            "points_spent, status, redeem_code) VALUES (#{userId}, #{productId}, #{productName}, " +
            "#{productImage}, #{pointsSpent}, #{status}, #{redeemCode})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExchangeRecord record);

    @Select("SELECT * FROM exchange_records WHERE id = #{id}")
    ExchangeRecord findById(Long id);
}
