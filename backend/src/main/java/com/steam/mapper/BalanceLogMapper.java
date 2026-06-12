package com.steam.mapper;

import com.steam.entity.BalanceLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BalanceLogMapper {

    @Select("SELECT * FROM balance_logs WHERE id = #{id}")
    BalanceLog findById(Long id);

    @Select("SELECT * FROM balance_logs WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<BalanceLog> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM balance_logs WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Select("SELECT * FROM balance_logs WHERE user_id = #{userId} AND source = #{source} AND source_id = #{sourceId} ORDER BY id DESC LIMIT 1")
    BalanceLog findByUserAndSource(@Param("userId") Long userId, @Param("source") String source, @Param("sourceId") String sourceId);

    @Insert("INSERT INTO balance_logs (user_id, type, amount, balance_after, source, source_id, description) " +
            "VALUES (#{userId}, #{type}, #{amount}, #{balanceAfter}, #{source}, #{sourceId}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BalanceLog log);

    @Select("SELECT * FROM balance_logs WHERE source = #{source} AND source_id = #{sourceId} ORDER BY id DESC LIMIT 1")
    BalanceLog findBySourceAndId(@Param("source") String source, @Param("sourceId") String sourceId);
}
