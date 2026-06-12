package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PointProduct {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Integer pointsRequired;
    private Integer stock;
    private Integer soldCount;
    private String type;
    private BigDecimal value;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
