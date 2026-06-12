package com.steam.mapper;

import com.steam.entity.Notification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper {
    
    @Insert("INSERT INTO notifications (user_id, type, game_id, title, content, " +
            "price_drop, price_drop_percent, old_price, new_price) VALUES (" +
            "#{userId}, #{type}, #{gameId}, #{title}, #{content}, " +
            "#{priceDrop}, #{priceDropPercent}, #{oldPrice}, #{newPrice})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);
    
    @Select("SELECT n.*, g.id as game_id, g.title, g.cover_image, g.original_price, " +
            "g.discount_price, g.discount_percent " +
            "FROM notifications n " +
            "LEFT JOIN games g ON n.game_id = g.id " +
            "WHERE n.user_id = #{userId} " +
            "ORDER BY n.created_at DESC " +
            "LIMIT #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "type", column = "type"),
        @Result(property = "gameId", column = "game_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "priceDrop", column = "price_drop"),
        @Result(property = "priceDropPercent", column = "price_drop_percent"),
        @Result(property = "oldPrice", column = "old_price"),
        @Result(property = "newPrice", column = "new_price"),
        @Result(property = "isRead", column = "is_read"),
        @Result(property = "readAt", column = "read_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "game.id", column = "game_id"),
        @Result(property = "game.title", column = "title"),
        @Result(property = "game.coverImage", column = "cover_image"),
        @Result(property = "game.originalPrice", column = "original_price"),
        @Result(property = "game.discountPrice", column = "discount_price"),
        @Result(property = "game.discountPercent", column = "discount_percent")
    })
    List<Notification> findByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    @Select("SELECT n.*, g.id as game_id, g.title, g.cover_image, g.original_price, " +
            "g.discount_price, g.discount_percent " +
            "FROM notifications n " +
            "LEFT JOIN games g ON n.game_id = g.id " +
            "WHERE n.user_id = #{userId} AND n.is_read = #{isRead} " +
            "ORDER BY n.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "type", column = "type"),
        @Result(property = "gameId", column = "game_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "priceDrop", column = "price_drop"),
        @Result(property = "priceDropPercent", column = "price_drop_percent"),
        @Result(property = "oldPrice", column = "old_price"),
        @Result(property = "newPrice", column = "new_price"),
        @Result(property = "isRead", column = "is_read"),
        @Result(property = "readAt", column = "read_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "game.id", column = "game_id"),
        @Result(property = "game.title", column = "title"),
        @Result(property = "game.coverImage", column = "cover_image"),
        @Result(property = "game.originalPrice", column = "original_price"),
        @Result(property = "game.discountPrice", column = "discount_price"),
        @Result(property = "game.discountPercent", column = "discount_percent")
    })
    List<Notification> findByUserIdAndReadStatus(@Param("userId") Long userId, @Param("isRead") Integer isRead);
    
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(Long userId);
    
    @Update("UPDATE notifications SET is_read = 1, read_at = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);
    
    @Update("UPDATE notifications SET is_read = 1, read_at = NOW() WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(Long userId);
    
    @Select("SELECT * FROM notifications WHERE user_id = #{userId} AND game_id = #{gameId} " +
            "AND type = #{type} AND is_read = 0 " +
            "ORDER BY created_at DESC LIMIT 1")
    Notification findUnreadByUserAndGameAndType(@Param("userId") Long userId, 
                                                 @Param("gameId") Long gameId, 
                                                 @Param("type") String type);
    
    @Delete("DELETE FROM notifications WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
