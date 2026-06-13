package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameAnswer {
    private Long id;
    private Long userId;
    private Long questionId;
    private Long gameId;
    private String content;
    private Integer likeCount;
    private Integer isAdopted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User user;
    private Integer isLiked;
}
