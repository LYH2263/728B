package com.steam.controller;

import com.steam.dto.PriceChartDTO;
import com.steam.dto.Result;
import com.steam.entity.Game;
import com.steam.entity.PriceHistory;
import com.steam.mapper.GameMapper;
import com.steam.service.PriceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {
    
    private final PriceHistoryService priceHistoryService;
    private final GameMapper gameMapper;
    
    @GetMapping("/game/{gameId}")
    public Result<List<PriceHistory>> getPriceHistory(@PathVariable Long gameId) {
        List<PriceHistory> history = priceHistoryService.getPriceHistory(gameId);
        return Result.success(history);
    }
    
    @GetMapping("/game/{gameId}/range")
    public Result<List<PriceHistory>> getPriceHistoryByRange(
            @PathVariable Long gameId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<PriceHistory> history = priceHistoryService.getPriceHistoryByTimeRange(gameId, startTime, endTime);
        return Result.success(history);
    }
    
    @GetMapping("/game/{gameId}/lowest")
    public Result<BigDecimal> getLowestPrice(@PathVariable Long gameId) {
        BigDecimal lowestPrice = priceHistoryService.getAllTimeLowestPrice(gameId);
        return Result.success(lowestPrice);
    }
    
    @GetMapping("/game/{gameId}/chart")
    public Result<PriceChartDTO> getPriceChart(
            @PathVariable Long gameId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        Game game = gameMapper.findById(gameId);
        if (game == null) {
            throw new RuntimeException("游戏不存在");
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        List<PriceHistory> history = priceHistoryService.getPriceHistoryByTimeRange(gameId, startTime, endTime);
        
        PriceChartDTO chartDTO = new PriceChartDTO();
        chartDTO.setOriginalPrice(game.getOriginalPrice());
        BigDecimal currentPrice = game.getDiscountPrice() != null ? game.getDiscountPrice() : game.getOriginalPrice();
        chartDTO.setCurrentPrice(currentPrice);
        
        List<PriceChartDTO.PricePoint> pricePoints = new ArrayList<>();
        BigDecimal initialPrice = game.getOriginalPrice();
        if (!history.isEmpty()) {
            PriceHistory first = history.get(0);
            initialPrice = first.getDiscountPrice() != null ? first.getDiscountPrice() : first.getOriginalPrice();
        }
        
        Map<LocalDate, BigDecimal> dailyPrices = new TreeMap<>();
        dailyPrices.put(startTime.toLocalDate(), initialPrice);
        
        for (PriceHistory h : history) {
            BigDecimal price = h.getNewDiscountPrice() != null ? h.getNewDiscountPrice() : h.getOriginalPrice();
            dailyPrices.put(h.getCreatedAt().toLocalDate(), price);
        }
        
        dailyPrices.put(endTime.toLocalDate(), currentPrice);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<BigDecimal> allPrices = new ArrayList<>();
        
        for (Map.Entry<LocalDate, BigDecimal> entry : dailyPrices.entrySet()) {
            PriceChartDTO.PricePoint point = new PriceChartDTO.PricePoint(
                    entry.getKey().atStartOfDay(), 
                    entry.getValue()
            );
            point.setDateLabel(entry.getKey().format(formatter));
            pricePoints.add(point);
            allPrices.add(entry.getValue());
        }
        
        chartDTO.setPricePoints(pricePoints);
        
        if (!allPrices.isEmpty()) {
            chartDTO.setLowestPrice(Collections.min(allPrices));
            chartDTO.setHighestPrice(Collections.max(allPrices));
            BigDecimal sum = allPrices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            chartDTO.setAveragePrice(sum.divide(BigDecimal.valueOf(allPrices.size()), 2, RoundingMode.HALF_UP));
        } else {
            chartDTO.setLowestPrice(currentPrice);
            chartDTO.setHighestPrice(currentPrice);
            chartDTO.setAveragePrice(currentPrice);
        }
        
        BigDecimal allTimeLowest = priceHistoryService.getAllTimeLowestPrice(gameId);
        if (allTimeLowest != null && chartDTO.getLowestPrice().compareTo(allTimeLowest) > 0) {
            chartDTO.setLowestPrice(allTimeLowest);
        }
        
        return Result.success(chartDTO);
    }
    
    @PostMapping("/record")
    public Result<PriceHistory> recordPriceChange(
            @RequestParam Long gameId,
            @RequestParam(required = false) BigDecimal oldDiscountPrice,
            @RequestParam(required = false) BigDecimal newDiscountPrice) {
        PriceHistory history = priceHistoryService.recordPriceChange(gameId, oldDiscountPrice, newDiscountPrice);
        if (history == null) {
            return Result.successMessage("价格未变化");
        }
        return Result.success(history);
    }
}
