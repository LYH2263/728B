package com.steam.mapper;

import com.steam.entity.GameAnswer;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface GameAnswerMapper {

    @Select("SELECT ga.*, u.username, u.nickname, u.avatar " +
            "FROM game_answers ga " +
            "INNER JOIN users u ON ga.user_id = u.id " +
            "WHERE ga.question_id = #{questionId} " +
            "ORDER BY ga.is_adopted DESC, ga.like_count DESC, ga.created_at ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "questionId", column = "question_id"),
            @Result(property = "gameId", column = "game_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "isAdopted", column = "is_adopted"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.nickname", column = "nickname"),
            @Result(property = "user.avatar", column = "avatar")
    })
    List<GameAnswer> findByQuestionId(@Param("questionId") Long questionId);

    @Select("SELECT ga.*, u.username, u.nickname, u.avatar, " +
            "CASE WHEN gal.id IS NOT NULL THEN 1 ELSE 0 END AS is_liked " +
            "FROM game_answers ga " +
            "INNER JOIN users u ON ga.user_id = u.id " +
            "LEFT JOIN game_answer_likes gal ON gal.answer_id = ga.id AND gal.user_id = #{userId} " +
            "WHERE ga.question_id = #{questionId} " +
            "ORDER BY ga.is_adopted DESC, ga.like_count DESC, ga.created_at ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "questionId", column = "question_id"),
            @Result(property = "gameId", column = "game_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "isAdopted", column = "is_adopted"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "isLiked", column = "is_liked"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.nickname", column = "nickname"),
            @Result(property = "user.avatar", column = "avatar")
    })
    List<GameAnswer> findByQuestionIdWithLikeStatus(@Param("questionId") Long questionId, @Param("userId") Long userId);

    @Select("SELECT * FROM game_answers WHERE id = #{id}")
    GameAnswer findById(@Param("id") Long id);

    @Insert("INSERT INTO game_answers (user_id, question_id, game_id, content) " +
            "VALUES (#{userId}, #{questionId}, #{gameId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GameAnswer answer);

    @Update("UPDATE game_answers SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(Long id);

    @Update("UPDATE game_answers SET like_count = like_count - 1 WHERE id = #{id}")
    int decrementLikeCount(Long id);

    @Update("UPDATE game_answers SET is_adopted = 1 WHERE id = #{id}")
    int markAdopted(Long id);

    @Update("UPDATE game_answers SET is_adopted = 0 WHERE question_id = #{questionId} AND is_adopted = 1")
    int cancelAdoptedByQuestionId(Long questionId);

    @Delete("DELETE FROM game_answers WHERE id = #{id}")
    int deleteById(Long id);
}
