package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlashSaleOrder {
    private Long id;
    private Long flashSaleId;
    private Long userId;
    private Long gameId;
    private Long orderId;
    private Integer quantity;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
