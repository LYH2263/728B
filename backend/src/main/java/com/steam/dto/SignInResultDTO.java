package com.steam.dto;

import lombok.Data;

@Data
public class SignInResultDTO {
    private Boolean signedToday;
    private Integer consecutiveDays;
    private Integer pointsEarned;
    private Integer bonusPoints;
    private Integer totalPoints;
    private String message;
}
