package com.steam.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserPoints {
    private Long id;
    private Long userId;
    private Integer balance;
    private Integer totalEarned;
    private Integer totalSpent;
    private Integer consecutiveDays;
    private LocalDate lastSignDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
