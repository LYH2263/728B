package com.steam.mapper;

import com.steam.entity.FlashSale;
import com.steam.entity.FlashSaleOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FlashSaleMapper {

    @Select("SELECT * FROM flash_sales WHERE id = #{id}")
    FlashSale findById(Long id);

    @Select("SELECT * FROM flash_sales WHERE id = #{id} FOR UPDATE")
    FlashSale findByIdForUpdate(Long id);

    List<FlashSale> findAllWithGames();

    @Update("UPDATE flash_sales SET stock_count = stock_count - 1, sold_count = sold_count + 1 " +
            "WHERE id = #{id} AND stock_count > 0")
    int decreaseStock(Long id);

    @Select("SELECT COALESCE(SUM(quantity), 0) FROM flash_sale_orders " +
            "WHERE flash_sale_id = #{flashSaleId} AND user_id = #{userId} AND status != 'CANCELLED'")
    int countUserPurchased(@Param("flashSaleId") Long flashSaleId, @Param("userId") Long userId);

    @Insert("INSERT INTO flash_sale_orders (flash_sale_id, user_id, game_id, quantity, price, status) " +
            "VALUES (#{flashSaleId}, #{userId}, #{gameId}, #{quantity}, #{price}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFlashSaleOrder(FlashSaleOrder order);

    @Update("UPDATE flash_sale_orders SET order_id = #{orderId}, status = 'PAID' WHERE id = #{id}")
    int updateOrderPaid(@Param("id") Long id, @Param("orderId") Long orderId);

    @Select("SELECT * FROM flash_sale_orders WHERE id = #{id}")
    FlashSaleOrder findFlashSaleOrderById(Long id);
}
