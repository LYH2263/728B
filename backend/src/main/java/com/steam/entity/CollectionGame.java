package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CollectionGame {
    private Long id;
    private Long collectionId;
    private Long gameId;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    private Game game;
    private BigDecimal currentPrice;
}
