package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 愿望单实体类
 */
@Data
public class Wishlist {
    private Long id;
    private Long userId;
    private Long gameId;
    private BigDecimal addedPrice;
    private BigDecimal addedOriginalPrice;
    private LocalDateTime createdAt;
    
    // 关联字段（非数据库字段）
    private Game game;
    private BigDecimal priceDrop;
    private Integer priceDropPercent;
    private BigDecimal lowestPrice;
    private BigDecimal currentPrice;
}
