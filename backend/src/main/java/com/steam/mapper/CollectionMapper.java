package com.steam.mapper;

import com.steam.entity.Collection;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CollectionMapper {

    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM collections c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.user_id = #{userId} " +
            "ORDER BY c.sort_order ASC, c.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "coverImages", column = "cover_images"),
        @Result(property = "isPublic", column = "is_public"),
        @Result(property = "gameCount", column = "game_count"),
        @Result(property = "totalPrice", column = "total_price"),
        @Result(property = "sortOrder", column = "sort_order"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    List<Collection> findByUserId(Long userId);

    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM collections c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "coverImages", column = "cover_images"),
        @Result(property = "isPublic", column = "is_public"),
        @Result(property = "gameCount", column = "game_count"),
        @Result(property = "totalPrice", column = "total_price"),
        @Result(property = "sortOrder", column = "sort_order"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    Collection findById(Long id);

    @Insert("INSERT INTO collections (user_id, name, description, is_public, sort_order) " +
            "VALUES (#{userId}, #{name}, #{description}, #{isPublic}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Collection collection);

    @Update("UPDATE collections SET name = #{name}, description = #{description}, " +
            "is_public = #{isPublic}, sort_order = #{sortOrder} WHERE id = #{id} AND user_id = #{userId}")
    int update(Collection collection);

    @Delete("DELETE FROM collections WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE collections SET cover_images = #{coverImages} WHERE id = #{id}")
    int updateCoverImages(@Param("id") Long id, @Param("coverImages") String coverImages);

    @Update("UPDATE collections SET game_count = #{gameCount}, total_price = #{totalPrice} WHERE id = #{id}")
    int updateStats(@Param("id") Long id, @Param("gameCount") int gameCount, @Param("totalPrice") java.math.BigDecimal totalPrice);

    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM collections c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.is_public = 1 " +
            "ORDER BY c.updated_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "coverImages", column = "cover_images"),
        @Result(property = "isPublic", column = "is_public"),
        @Result(property = "gameCount", column = "game_count"),
        @Result(property = "totalPrice", column = "total_price"),
        @Result(property = "sortOrder", column = "sort_order"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    List<Collection> findPublicCollections(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM collections WHERE is_public = 1")
    int countPublicCollections();

    @Select("SELECT COUNT(*) FROM collections WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
