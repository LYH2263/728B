package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeOrder {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long planId;
    private String planName;
    private BigDecimal amount;
    private BigDecimal bonusAmount;
    private BigDecimal totalAmount;
    private String payMethod;
    private String status;
    private LocalDateTime payTime;
    private LocalDateTime expireTime;
    private String transactionId;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
