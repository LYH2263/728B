package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceHistory {
    private Long id;
    private Long gameId;
    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
    private BigDecimal newDiscountPrice;
    private Integer discountPercent;
    private BigDecimal priceChange;
    private BigDecimal changePercent;
    private LocalDateTime createdAt;
    
    private Game game;
}
