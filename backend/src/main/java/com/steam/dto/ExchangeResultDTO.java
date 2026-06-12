package com.steam.dto;

import lombok.Data;

@Data
public class ExchangeResultDTO {
    private Long recordId;
    private String productName;
    private String productImage;
    private Integer pointsSpent;
    private Integer remainingPoints;
    private String redeemCode;
    private String message;
}
