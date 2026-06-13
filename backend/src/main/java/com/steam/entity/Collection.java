package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Collection {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String coverImages;
    private Integer isPublic;
    private Integer gameCount;
    private BigDecimal totalPrice;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String username;
    private String userAvatar;
}
