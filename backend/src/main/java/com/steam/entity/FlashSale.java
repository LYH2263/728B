package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlashSale {
    private Long id;
    private Long gameId;
    private BigDecimal flashPrice;
    private Integer stockCount;
    private Integer soldCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer perUserLimit;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private transient Game game;

    private transient String activityStatus;
}
