package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointLog {
    private Long id;
    private Long userId;
    private String type;
    private Integer amount;
    private Integer balanceAfter;
    private String source;
    private String sourceId;
    private String description;
    private LocalDateTime createdAt;
}
