package com.steam.mapper;

import com.steam.entity.PointProduct;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PointProductMapper {

    @Select("SELECT * FROM point_products WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<PointProduct> findAllActive();

    @Select("SELECT * FROM point_products WHERE id = #{id}")
    PointProduct findById(Long id);

    @Update("UPDATE point_products SET stock = stock - 1, sold_count = sold_count + 1, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND stock > 0")
    int decreaseStock(Long id);

    @Update("UPDATE point_products SET stock = stock + 1, sold_count = sold_count - 1, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int increaseStock(Long id);
}
