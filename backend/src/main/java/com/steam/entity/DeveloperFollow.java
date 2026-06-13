package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeveloperFollow {
    private Long id;
    private Long userId;
    private Long developerId;
    private LocalDateTime createdAt;

    private Developer developer;
}
