package com.steam.mapper;

import com.steam.entity.RechargePlan;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RechargePlanMapper {

    @Select("SELECT * FROM recharge_plans WHERE id = #{id}")
    RechargePlan findById(Long id);

    @Select("SELECT * FROM recharge_plans WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<RechargePlan> findAllActive();

    @Select("SELECT * FROM recharge_plans ORDER BY sort_order ASC, id ASC")
    List<RechargePlan> findAll();

    @Insert("INSERT INTO recharge_plans (name, amount, bonus_amount, description, icon, tag, tag_color, sort_order, status) " +
            "VALUES (#{name}, #{amount}, #{bonusAmount}, #{description}, #{icon}, #{tag}, #{tagColor}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RechargePlan plan);

    @Update("UPDATE recharge_plans SET name = #{name}, amount = #{amount}, bonus_amount = #{bonusAmount}, " +
            "description = #{description}, icon = #{icon}, tag = #{tag}, tag_color = #{tagColor}, " +
            "sort_order = #{sortOrder}, status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(RechargePlan plan);

    @Delete("DELETE FROM recharge_plans WHERE id = #{id}")
    int deleteById(Long id);
}
