package com.steam.mapper;

import com.steam.entity.PriceHistory;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PriceHistoryMapper {
    
    @Insert("INSERT INTO price_history (game_id, original_price, discount_price, new_discount_price, " +
            "discount_percent, price_change, change_percent) VALUES (" +
            "#{gameId}, #{originalPrice}, #{discountPrice}, #{newDiscountPrice}, " +
            "#{discountPercent}, #{priceChange}, #{changePercent})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PriceHistory priceHistory);
    
    @Select("SELECT ph.*, g.id as game_id, g.title, g.cover_image " +
            "FROM price_history ph " +
            "LEFT JOIN games g ON ph.game_id = g.id " +
            "WHERE ph.game_id = #{gameId} " +
            "ORDER BY ph.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "gameId", column = "game_id"),
        @Result(property = "originalPrice", column = "original_price"),
        @Result(property = "discountPrice", column = "discount_price"),
        @Result(property = "newDiscountPrice", column = "new_discount_price"),
        @Result(property = "discountPercent", column = "discount_percent"),
        @Result(property = "priceChange", column = "price_change"),
        @Result(property = "changePercent", column = "change_percent"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "game.id", column = "game_id"),
        @Result(property = "game.title", column = "title"),
        @Result(property = "game.coverImage", column = "cover_image")
    })
    List<PriceHistory> findByGameId(Long gameId);
    
    @Select("SELECT ph.* FROM price_history ph " +
            "WHERE ph.game_id = #{gameId} " +
            "ORDER BY ph.created_at DESC " +
            "LIMIT 1")
    PriceHistory findLatestByGameId(Long gameId);
    
    @Select("SELECT COALESCE(MIN(CASE " +
            "WHEN ph.new_discount_price IS NOT NULL THEN ph.new_discount_price " +
            "ELSE ph.original_price END), #{defaultPrice}) " +
            "FROM price_history ph " +
            "WHERE ph.game_id = #{gameId} " +
            "AND ph.created_at >= #{startTime}")
    BigDecimal findLowestPriceByGameIdAndTimeRange(@Param("gameId") Long gameId, 
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("defaultPrice") BigDecimal defaultPrice);
    
    @Select("SELECT COALESCE(MIN(CASE " +
            "WHEN ph.new_discount_price IS NOT NULL THEN ph.new_discount_price " +
            "ELSE ph.original_price END), g.original_price) " +
            "FROM price_history ph " +
            "RIGHT JOIN games g ON ph.game_id = g.id " +
            "WHERE g.id = #{gameId}")
    BigDecimal findAllTimeLowestPrice(Long gameId);
    
    @Select("SELECT ph.* FROM price_history ph " +
            "WHERE ph.game_id = #{gameId} " +
            "AND ph.created_at BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY ph.created_at ASC")
    List<PriceHistory> findByGameIdAndTimeRange(@Param("gameId") Long gameId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
}
