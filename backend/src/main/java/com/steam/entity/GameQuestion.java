package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameQuestion {
    private Long id;
    private Long userId;
    private Long gameId;
    private String title;
    private String content;
    private Integer answerCount;
    private Integer viewCount;
    private Integer isResolved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User user;
}
