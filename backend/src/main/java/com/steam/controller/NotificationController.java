package com.steam.controller;

import com.steam.dto.Result;
import com.steam.entity.Notification;
import com.steam.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    public Result<List<Notification>> getNotifications(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {
        Long userId = (Long) request.getAttribute("userId");
        List<Notification> notifications = notificationService.getNotifications(userId, limit);
        return Result.success(notifications);
    }
    
    @GetMapping("/unread")
    public Result<List<Notification>> getUnreadNotifications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return Result.success(notifications);
    }
    
    @GetMapping("/count")
    public Result<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = notificationService.getUnreadCount(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return Result.success(result);
    }
    
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAsRead(id, userId);
        return Result.successMessage("已标记为已读");
    }
    
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return Result.successMessage("全部标记为已读");
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.deleteNotification(id, userId);
        return Result.successMessage("已删除");
    }
}
