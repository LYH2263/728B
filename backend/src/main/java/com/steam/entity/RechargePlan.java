package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargePlan {
    private Long id;
    private String name;
    private BigDecimal amount;
    private BigDecimal bonusAmount;
    private String description;
    private String icon;
    private String tag;
    private String tagColor;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
