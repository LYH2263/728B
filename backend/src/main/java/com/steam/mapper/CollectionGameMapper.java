package com.steam.mapper;

import com.steam.entity.CollectionGame;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CollectionGameMapper {

    @Select("SELECT cg.*, g.id as game_id, g.title, g.cover_image, g.original_price, " +
            "g.discount_price, g.discount_percent, g.rating, " +
            "COALESCE(g.discount_price, g.original_price) as current_price " +
            "FROM collection_games cg " +
            "INNER JOIN games g ON cg.game_id = g.id " +
            "WHERE cg.collection_id = #{collectionId} AND g.status = 1 " +
            "ORDER BY cg.sort_order ASC, cg.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "collectionId", column = "collection_id"),
        @Result(property = "gameId", column = "game_id"),
        @Result(property = "sortOrder", column = "sort_order"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "currentPrice", column = "current_price"),
        @Result(property = "game.id", column = "game_id"),
        @Result(property = "game.title", column = "title"),
        @Result(property = "game.coverImage", column = "cover_image"),
        @Result(property = "game.originalPrice", column = "original_price"),
        @Result(property = "game.discountPrice", column = "discount_price"),
        @Result(property = "game.discountPercent", column = "discount_percent"),
        @Result(property = "game.rating", column = "rating")
    })
    List<CollectionGame> findByCollectionId(Long collectionId);

    @Insert("INSERT INTO collection_games (collection_id, game_id, sort_order) " +
            "VALUES (#{collectionId}, #{gameId}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CollectionGame collectionGame);

    @Delete("DELETE FROM collection_games WHERE collection_id = #{collectionId} AND game_id = #{gameId}")
    int deleteByCollectionIdAndGameId(@Param("collectionId") Long collectionId, @Param("gameId") Long gameId);

    @Select("SELECT EXISTS(SELECT 1 FROM collection_games WHERE collection_id = #{collectionId} AND game_id = #{gameId})")
    boolean existsByCollectionIdAndGameId(@Param("collectionId") Long collectionId, @Param("gameId") Long gameId);

    @Delete("DELETE FROM collection_games WHERE collection_id = #{collectionId}")
    int deleteByCollectionId(Long collectionId);

    @Select("SELECT cg.collection_id FROM collection_games cg WHERE cg.game_id = #{gameId}")
    List<Long> findCollectionIdsByGameId(Long gameId);

    @Select("SELECT cg.* FROM collection_games cg " +
            "INNER JOIN collections c ON cg.collection_id = c.id " +
            "WHERE c.user_id = #{userId} AND cg.game_id = #{gameId}")
    List<CollectionGame> findByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);

    @Update("UPDATE collection_games SET sort_order = #{sortOrder} " +
            "WHERE collection_id = #{collectionId} AND game_id = #{gameId}")
    int updateSortOrder(@Param("collectionId") Long collectionId, @Param("gameId") Long gameId, @Param("sortOrder") int sortOrder);

    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM collection_games WHERE collection_id = #{collectionId}")
    int maxSortOrder(Long collectionId);
}
