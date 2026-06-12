package com.steam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PriceChartDTO {
    private List<PricePoint> pricePoints;
    private BigDecimal currentPrice;
    private BigDecimal originalPrice;
    private BigDecimal lowestPrice;
    private BigDecimal highestPrice;
    private BigDecimal averagePrice;
    
    @Data
    public static class PricePoint {
        private LocalDateTime date;
        private BigDecimal price;
        private String dateLabel;
        
        public PricePoint(LocalDateTime date, BigDecimal price) {
            this.date = date;
            this.price = price;
        }
    }
}
