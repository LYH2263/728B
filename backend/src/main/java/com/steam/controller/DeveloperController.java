package com.steam.controller;

import com.steam.dto.Result;
import com.steam.entity.Developer;
import com.steam.entity.DeveloperFollow;
import com.steam.service.DeveloperService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/developers")
@RequiredArgsConstructor
public class DeveloperController {

    private final DeveloperService developerService;

    @GetMapping
    public Result<List<Developer>> getAllDevelopers() {
        List<Developer> developers = developerService.getAllDevelopers();
        return Result.success(developers);
    }

    @GetMapping("/top")
    public Result<List<Developer>> getTopDevelopers(@RequestParam(defaultValue = "10") Integer limit) {
        List<Developer> developers = developerService.getTopDevelopers(limit);
        return Result.success(developers);
    }

    @GetMapping("/search")
    public Result<List<Developer>> searchDevelopers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") Integer limit) {
        List<Developer> developers = developerService.searchDevelopers(keyword, limit);
        return Result.success(developers);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDeveloperDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> detail = developerService.getDeveloperDetail(id);

        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) {
            boolean isFollowing = developerService.isFollowing(userId, id);
            detail.put("isFollowing", isFollowing);
        } else {
            detail.put("isFollowing", false);
        }

        return Result.success(detail);
    }

    @PostMapping("/{id}/follow")
    public Result<Void> followDeveloper(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        developerService.followDeveloper(userId, id);
        return Result.successMessage("关注成功");
    }

    @DeleteMapping("/{id}/follow")
    public Result<Void> unfollowDeveloper(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        developerService.unfollowDeveloper(userId, id);
        return Result.successMessage("已取消关注");
    }

    @GetMapping("/{id}/follow-status")
    public Result<Map<String, Object>> getFollowStatus(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        if (userId != null) {
            result.put("isFollowing", developerService.isFollowing(userId, id));
        } else {
            result.put("isFollowing", false);
        }
        return Result.success(result);
    }

    @GetMapping("/followed")
    public Result<List<DeveloperFollow>> getFollowedDevelopers(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<DeveloperFollow> follows = developerService.getFollowedDevelopers(userId);
        return Result.success(follows);
    }

    @GetMapping("/followed/count")
    public Result<Map<String, Object>> getFollowedCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = developerService.getFollowedCount(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }
}
