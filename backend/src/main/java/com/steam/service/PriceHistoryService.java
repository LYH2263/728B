package com.steam.service;

import com.steam.entity.Game;
import com.steam.entity.PriceHistory;
import com.steam.entity.Wishlist;
import com.steam.mapper.GameMapper;
import com.steam.mapper.PriceHistoryMapper;
import com.steam.mapper.WishlistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceHistoryService {
    
    private final PriceHistoryMapper priceHistoryMapper;
    private final GameMapper gameMapper;
    private final WishlistMapper wishlistMapper;
    private final NotificationService notificationService;
    
    public List<PriceHistory> getPriceHistory(Long gameId) {
        return priceHistoryMapper.findByGameId(gameId);
    }
    
    public List<PriceHistory> getPriceHistoryByTimeRange(Long gameId, LocalDateTime startTime, LocalDateTime endTime) {
        return priceHistoryMapper.findByGameIdAndTimeRange(gameId, startTime, endTime);
    }
    
    public BigDecimal getAllTimeLowestPrice(Long gameId) {
        return priceHistoryMapper.findAllTimeLowestPrice(gameId);
    }
    
    public BigDecimal getLowestPriceSinceDate(Long gameId, LocalDateTime sinceDate, BigDecimal defaultPrice) {
        return priceHistoryMapper.findLowestPriceByGameIdAndTimeRange(gameId, sinceDate, defaultPrice);
    }
    
    @Transactional
    public PriceHistory recordPriceChange(Long gameId, BigDecimal oldDiscountPrice, BigDecimal newDiscountPrice) {
        Game game = gameMapper.findById(gameId);
        if (game == null) {
            throw new RuntimeException("游戏不存在");
        }
        
        BigDecimal oldPrice = oldDiscountPrice != null ? oldDiscountPrice : game.getOriginalPrice();
        BigDecimal newPrice = newDiscountPrice != null ? newDiscountPrice : game.getOriginalPrice();
        
        if (oldPrice.compareTo(newPrice) == 0) {
            log.info("游戏 {} 价格未变化，跳过记录", gameId);
            return null;
        }
        
        BigDecimal priceChange = oldPrice.subtract(newPrice);
        BigDecimal changePercent = priceChange.divide(oldPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        
        PriceHistory history = new PriceHistory();
        history.setGameId(gameId);
        history.setOriginalPrice(game.getOriginalPrice());
        history.setDiscountPrice(oldDiscountPrice);
        history.setNewDiscountPrice(newDiscountPrice);
        history.setDiscountPercent(game.getDiscountPercent());
        history.setPriceChange(priceChange);
        history.setChangePercent(changePercent);
        
        priceHistoryMapper.insert(history);
        log.info("记录游戏 {} 价格变动: {} -> {}, 变动: {}", gameId, oldPrice, newPrice, priceChange);
        
        if (priceChange.compareTo(BigDecimal.ZERO) > 0) {
            notifyWishlistUsers(gameId, oldPrice, newPrice, changePercent.intValue());
        }
        
        return history;
    }
    
    private void notifyWishlistUsers(Long gameId, BigDecimal oldPrice, BigDecimal newPrice, int dropPercent) {
        List<Long> userIds = wishlistMapper.findUserIdsByGameId(gameId);
        if (userIds == null || userIds.isEmpty()) {
            log.info("游戏 {} 没有被加入愿望单，跳过通知", gameId);
            return;
        }
        
        Game game = gameMapper.findById(gameId);
        if (game == null) {
            return;
        }
        
        for (Long userId : userIds) {
            try {
                Wishlist wishlist = wishlistMapper.findByUserIdAndGameId(userId, gameId);
                BigDecimal addedPrice = wishlist.getAddedPrice() != null ? 
                        wishlist.getAddedPrice() : oldPrice;
                
                BigDecimal priceDrop = addedPrice.subtract(newPrice);
                if (priceDrop.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                
                int priceDropPercent = priceDrop.divide(addedPrice, 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).intValue();
                
                notificationService.createPriceDropNotification(
                        userId, gameId, game.getTitle(),
                        addedPrice, newPrice, priceDrop, priceDropPercent
                );
            } catch (Exception e) {
                log.error("为用户 {} 创建价格变动通知失败", userId, e);
            }
        }
    }
}
