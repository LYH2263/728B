package com.steam.service;

import com.steam.entity.Notification;
import com.steam.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationMapper notificationMapper;
    
    private static final String TYPE_PRICE_DROP = "PRICE_DROP";
    
    public List<Notification> getNotifications(Long userId, Integer limit) {
        return notificationMapper.findByUserId(userId, limit != null ? limit : 50);
    }
    
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationMapper.findByUserIdAndReadStatus(userId, 0);
    }
    
    public List<Notification> getReadNotifications(Long userId) {
        return notificationMapper.findByUserIdAndReadStatus(userId, 1);
    }
    
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }
    
    @Transactional
    public void markAsRead(Long id, Long userId) {
        int rows = notificationMapper.markAsRead(id, userId);
        if (rows == 0) {
            log.warn("标记通知已读失败，通知不存在或无权限: id={}, userId={}", id, userId);
        }
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }
    
    @Transactional
    public void deleteNotification(Long id, Long userId) {
        int rows = notificationMapper.deleteByIdAndUserId(id, userId);
        if (rows == 0) {
            log.warn("删除通知失败，通知不存在或无权限: id={}, userId={}", id, userId);
        }
    }
    
    @Transactional
    public Notification createPriceDropNotification(Long userId, Long gameId, String gameTitle,
                                                    BigDecimal oldPrice, BigDecimal newPrice,
                                                    BigDecimal priceDrop, int priceDropPercent) {
        Notification existing = notificationMapper.findUnreadByUserAndGameAndType(
                userId, gameId, TYPE_PRICE_DROP);
        
        if (existing != null && existing.getNewPrice() != null 
                && existing.getNewPrice().compareTo(newPrice) <= 0) {
            log.info("用户 {} 已有游戏 {} 的未读降价通知，且新价格不低于现有通知价格，跳过", userId, gameId);
            return null;
        }
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(TYPE_PRICE_DROP);
        notification.setGameId(gameId);
        notification.setTitle(gameTitle + " 降价啦！");
        notification.setContent(String.format("%s 已降价 ¥%.2f，降幅 %d%%，现价 ¥%.2f", 
                gameTitle, priceDrop, priceDropPercent, newPrice));
        notification.setPriceDrop(priceDrop);
        notification.setPriceDropPercent(priceDropPercent);
        notification.setOldPrice(oldPrice);
        notification.setNewPrice(newPrice);
        notification.setIsRead(0);
        
        notificationMapper.insert(notification);
        log.info("为用户 {} 创建游戏 {} 降价通知: {}%", userId, gameId, priceDropPercent);
        
        return notification;
    }
}
