package com.steam.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface GameAnswerLikeMapper {

    @Select("SELECT COUNT(*) FROM game_answer_likes WHERE user_id = #{userId} AND answer_id = #{answerId}")
    int existsByUserIdAndAnswerId(@Param("userId") Long userId, @Param("answerId") Long answerId);

    @Insert("INSERT INTO game_answer_likes (user_id, answer_id) VALUES (#{userId}, #{answerId})")
    int insert(@Param("userId") Long userId, @Param("answerId") Long answerId);

    @Delete("DELETE FROM game_answer_likes WHERE user_id = #{userId} AND answer_id = #{answerId}")
    int delete(@Param("userId") Long userId, @Param("answerId") Long answerId);
}
