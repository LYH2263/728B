package com.steam.service;

import com.steam.entity.Game;
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
import java.util.List;

/**
 * 愿望单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {
    
    private final WishlistMapper wishlistMapper;
    private final GameMapper gameMapper;
    private final PriceHistoryMapper priceHistoryMapper;
    
    /**
     * 获取用户愿望单
     */
    public List<Wishlist> getWishlist(Long userId) {
        return wishlistMapper.findByUserId(userId);
    }
    
    /**
     * 获取用户愿望单（包含价格变动信息）
     */
    public List<Wishlist> getWishlistWithPriceInfo(Long userId) {
        List<Wishlist> items = wishlistMapper.findByUserIdWithPriceInfo(userId);
        
        for (Wishlist item : items) {
            Game game = item.getGame();
            if (game == null) continue;
            
            BigDecimal currentPrice = item.getCurrentPrice();
            BigDecimal addedPrice = item.getAddedPrice();
            
            if (addedPrice != null && currentPrice != null) {
                BigDecimal priceDrop = addedPrice.subtract(currentPrice);
                if (priceDrop.compareTo(BigDecimal.ZERO) > 0) {
                    item.setPriceDrop(priceDrop);
                    int dropPercent = priceDrop.divide(addedPrice, 2, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")).intValue();
                    item.setPriceDropPercent(dropPercent);
                }
            }
            
            BigDecimal lowestPrice = priceHistoryMapper.findAllTimeLowestPrice(game.getId());
            item.setLowestPrice(lowestPrice);
        }
        
        return items;
    }
    
    /**
     * 添加游戏到愿望单
     */
    @Transactional
    public void addToWishlist(Long userId, Long gameId) {
        // 检查游戏是否存在
        Game game = gameMapper.findById(gameId);
        if (game == null) {
            throw new RuntimeException("游戏不存在");
        }
        
        // 检查是否已在愿望单
        if (wishlistMapper.existsByUserIdAndGameId(userId, gameId)) {
            throw new RuntimeException("游戏已在愿望单中");
        }
        
        BigDecimal addedPrice = game.getDiscountPrice() != null ? 
                game.getDiscountPrice() : game.getOriginalPrice();
        
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setGameId(gameId);
        wishlist.setAddedPrice(addedPrice);
        wishlist.setAddedOriginalPrice(game.getOriginalPrice());
        
        wishlistMapper.insert(wishlist);
        log.info("用户 {} 添加游戏 {} 到愿望单，价格: {}", userId, gameId, addedPrice);
    }
    
    /**
     * 从愿望单移除游戏
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long gameId) {
        int rows = wishlistMapper.deleteByUserIdAndGameId(userId, gameId);
        if (rows == 0) {
            throw new RuntimeException("愿望单中没有该游戏");
        }
        log.info("用户 {} 从愿望单移除游戏 {}", userId, gameId);
    }
    
    /**
     * 检查游戏是否在愿望单中
     */
    public boolean isInWishlist(Long userId, Long gameId) {
        return wishlistMapper.existsByUserIdAndGameId(userId, gameId);
    }
    
    /**
     * 获取愿望单数量
     */
    public int getWishlistCount(Long userId) {
        return wishlistMapper.countByUserId(userId);
    }
}
