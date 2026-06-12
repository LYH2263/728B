package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExchangeRecord {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer pointsSpent;
    private String status;
    private String redeemCode;
    private LocalDateTime createdAt;

    private PointProduct product;
}
