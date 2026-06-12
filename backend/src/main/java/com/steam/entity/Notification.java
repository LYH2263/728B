package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private Long userId;
    private String type;
    private Long gameId;
    private String title;
    private String content;
    private BigDecimal priceDrop;
    private Integer priceDropPercent;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private Integer isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    
    private Game game;
}
