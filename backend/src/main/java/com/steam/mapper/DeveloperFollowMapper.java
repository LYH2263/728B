package com.steam.mapper;

import com.steam.entity.DeveloperFollow;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DeveloperFollowMapper {

    @Insert("INSERT INTO developer_follows (user_id, developer_id) VALUES (#{userId}, #{developerId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DeveloperFollow developerFollow);

    @Delete("DELETE FROM developer_follows WHERE user_id = #{userId} AND developer_id = #{developerId}")
    int deleteByUserIdAndDeveloperId(@Param("userId") Long userId, @Param("developerId") Long developerId);

    @Select("SELECT EXISTS(SELECT 1 FROM developer_follows WHERE user_id = #{userId} AND developer_id = #{developerId})")
    boolean existsByUserIdAndDeveloperId(@Param("userId") Long userId, @Param("developerId") Long developerId);

    @Select("SELECT df.*, d.id as dev_id, d.name, d.avatar, d.description, d.country, d.founded_year, d.website, " +
            "d.follower_count, d.game_count " +
            "FROM developer_follows df " +
            "INNER JOIN developers d ON df.developer_id = d.id " +
            "WHERE df.user_id = #{userId} " +
            "ORDER BY df.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "developerId", column = "developer_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "developer.id", column = "dev_id"),
        @Result(property = "developer.name", column = "name"),
        @Result(property = "developer.avatar", column = "avatar"),
        @Result(property = "developer.description", column = "description"),
        @Result(property = "developer.country", column = "country"),
        @Result(property = "developer.foundedYear", column = "founded_year"),
        @Result(property = "developer.website", column = "website"),
        @Result(property = "developer.followerCount", column = "follower_count"),
        @Result(property = "developer.gameCount", column = "game_count")
    })
    List<DeveloperFollow> findByUserId(Long userId);

    @Select("SELECT user_id FROM developer_follows WHERE developer_id = #{developerId}")
    List<Long> findFollowerUserIds(Long developerId);

    @Select("SELECT COUNT(*) FROM developer_follows WHERE developer_id = #{developerId}")
    int countFollowers(Long developerId);

    @Select("SELECT COUNT(*) FROM developer_follows WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
