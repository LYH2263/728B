package com.steam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlashSaleOrderDTO {
    @NotNull(message = "秒杀活动ID不能为空")
    private Long flashSaleId;
}
