package com.steam.mapper;

import com.steam.entity.PointLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PointLogMapper {

    @Select("SELECT * FROM point_logs WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<PointLog> findByUserId(@Param("userId") Long userId,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM point_logs WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Insert("INSERT INTO point_logs (user_id, type, amount, balance_after, source, source_id, description) " +
            "VALUES (#{userId}, #{type}, #{amount}, #{balanceAfter}, #{source}, #{sourceId}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PointLog pointLog);

    @Select("SELECT * FROM point_logs WHERE user_id = #{userId} AND source = #{source} AND source_id = #{sourceId} LIMIT 1")
    PointLog findByUserAndSource(@Param("userId") Long userId,
                                 @Param("source") String source,
                                 @Param("sourceId") String sourceId);
}
