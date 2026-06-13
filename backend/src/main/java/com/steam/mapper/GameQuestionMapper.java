package com.steam.mapper;

import com.steam.entity.GameQuestion;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface GameQuestionMapper {

    @Select("SELECT gq.*, u.username, u.nickname, u.avatar " +
            "FROM game_questions gq " +
            "INNER JOIN users u ON gq.user_id = u.id " +
            "WHERE gq.game_id = #{gameId} " +
            "ORDER BY gq.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "gameId", column = "game_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "answerCount", column = "answer_count"),
            @Result(property = "viewCount", column = "view_count"),
            @Result(property = "isResolved", column = "is_resolved"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.nickname", column = "nickname"),
            @Result(property = "user.avatar", column = "avatar")
    })
    List<GameQuestion> findByGameId(@Param("gameId") Long gameId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT gq.*, u.username, u.nickname, u.avatar " +
            "FROM game_questions gq " +
            "INNER JOIN users u ON gq.user_id = u.id " +
            "WHERE gq.game_id = #{gameId} AND gq.is_resolved = #{isResolved} " +
            "ORDER BY gq.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "gameId", column = "game_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "answerCount", column = "answer_count"),
            @Result(property = "viewCount", column = "view_count"),
            @Result(property = "isResolved", column = "is_resolved"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.nickname", column = "nickname"),
            @Result(property = "user.avatar", column = "avatar")
    })
    List<GameQuestion> findByGameIdAndResolved(@Param("gameId") Long gameId, @Param("isResolved") Integer isResolved, @Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT gq.*, u.username, u.nickname, u.avatar " +
            "FROM game_questions gq " +
            "INNER JOIN users u ON gq.user_id = u.id " +
            "WHERE gq.game_id = #{gameId} " +
            "ORDER BY (gq.answer_count + gq.view_count) DESC, gq.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "gameId", column = "game_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "answerCount", column = "answer_count"),
            @Result(property = "viewCount", column = "view_count"),
            @Result(property = "isResolved", column = "is_resolved"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.nickname", column = "nickname"),
            @Result(property = "user.avatar", column = "avatar")
    })
    List<GameQuestion> findHotByGameId(@Param("gameId") Long gameId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM game_questions WHERE game_id = #{gameId}")
    Long countByGameId(Long gameId);

    @Select("SELECT COUNT(*) FROM game_questions WHERE game_id = #{gameId} AND is_resolved = #{isResolved}")
    Long countByGameIdAndResolved(@Param("gameId") Long gameId, @Param("isResolved") Integer isResolved);

    @Select("SELECT gq.*, u.username, u.nickname, u.avatar " +
            "FROM game_questions gq " +
            "INNER JOIN users u ON gq.user_id = u.id " +
            "WHERE gq.id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "gameId", column = "game_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "answerCount", column = "answer_count"),
            @Result(property = "viewCount", column = "view_count"),
            @Result(property = "isResolved", column = "is_resolved"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.nickname", column = "nickname"),
            @Result(property = "user.avatar", column = "avatar")
    })
    GameQuestion findById(@Param("id") Long id);

    @Insert("INSERT INTO game_questions (user_id, game_id, title, content) " +
            "VALUES (#{userId}, #{gameId}, #{title}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GameQuestion question);

    @Update("UPDATE game_questions SET answer_count = answer_count + 1 WHERE id = #{id}")
    int incrementAnswerCount(Long id);

    @Update("UPDATE game_questions SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);

    @Update("UPDATE game_questions SET is_resolved = 1 WHERE id = #{id}")
    int markResolved(Long id);

    @Delete("DELETE FROM game_questions WHERE id = #{id}")
    int deleteById(Long id);
}
