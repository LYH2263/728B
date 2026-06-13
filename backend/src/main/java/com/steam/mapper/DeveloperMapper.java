package com.steam.mapper;

import com.steam.entity.Developer;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DeveloperMapper {

    @Select("SELECT * FROM developers WHERE id = #{id}")
    Developer findById(Long id);

    @Select("SELECT * FROM developers WHERE name = #{name}")
    Developer findByName(String name);

    @Select("SELECT * FROM developers ORDER BY follower_count DESC, game_count DESC")
    List<Developer> findAll();

    @Select("SELECT * FROM developers ORDER BY follower_count DESC, game_count DESC LIMIT #{limit}")
    List<Developer> findTop(Integer limit);

    @Select("SELECT * FROM developers WHERE name LIKE CONCAT('%', #{keyword}, '%') ORDER BY follower_count DESC LIMIT #{limit}")
    List<Developer> searchByName(@Param("keyword") String keyword, @Param("limit") Integer limit);

    @Insert("INSERT INTO developers (name, avatar, description, country, founded_year, website, follower_count, game_count) " +
            "VALUES (#{name}, #{avatar}, #{description}, #{country}, #{foundedYear}, #{website}, #{followerCount}, #{gameCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Developer developer);

    @Update("UPDATE developers SET follower_count = #{followerCount}, game_count = #{gameCount} WHERE id = #{id}")
    int updateCounts(@Param("id") Long id, @Param("followerCount") Integer followerCount, @Param("gameCount") Integer gameCount);

    @Update("UPDATE developers SET follower_count = follower_count + 1 WHERE id = #{id}")
    int incrementFollowerCount(Long id);

    @Update("UPDATE developers SET follower_count = CASE WHEN follower_count > 0 THEN follower_count - 1 ELSE 0 END WHERE id = #{id}")
    int decrementFollowerCount(Long id);

    @Select("SELECT COUNT(*) FROM developer_follows WHERE developer_id = #{developerId}")
    int countFollowers(Long developerId);

    @Select("SELECT d.* FROM developers d " +
            "INNER JOIN games g ON g.developer = d.name " +
            "WHERE g.id = #{gameId} LIMIT 1")
    Developer findByGameId(Long gameId);
}
