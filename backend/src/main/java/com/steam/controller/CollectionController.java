package com.steam.controller;

import com.steam.dto.Result;
import com.steam.entity.Collection;
import com.steam.entity.CollectionGame;
import com.steam.service.CollectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping("/my")
    public Result<List<Collection>> getMyCollections(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(collectionService.getUserCollections(userId));
    }

    @GetMapping("/{id}")
    public Result<Collection> getCollectionDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(collectionService.getCollectionDetail(id, userId));
    }

    @GetMapping("/{id}/public")
    public Result<Collection> getCollectionDetailPublic(@PathVariable Long id) {
        return Result.success(collectionService.getCollectionDetailPublic(id));
    }

    @PostMapping
    public Result<Collection> createCollection(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        Integer isPublic = body.get("isPublic") != null ? ((Number) body.get("isPublic")).intValue() : 0;
        if (name == null || name.trim().isEmpty()) {
            return Result.error("合集名称不能为空");
        }
        return Result.success(collectionService.createCollection(userId, name.trim(), description, isPublic));
    }

    @PutMapping("/{id}")
    public Result<Collection> updateCollection(HttpServletRequest request, @PathVariable Long id,
                                               @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        Integer isPublic = body.get("isPublic") != null ? ((Number) body.get("isPublic")).intValue() : null;
        return Result.success(collectionService.updateCollection(userId, id, name, description, isPublic));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCollection(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        collectionService.deleteCollection(userId, id);
        return Result.successMessage("合集已删除");
    }

    @GetMapping("/{id}/games")
    public Result<List<CollectionGame>> getCollectionGames(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(collectionService.getCollectionGames(id, userId));
    }

    @GetMapping("/{id}/games/public")
    public Result<List<CollectionGame>> getCollectionGamesPublic(@PathVariable Long id) {
        return Result.success(collectionService.getCollectionGamesPublic(id));
    }

    @PostMapping("/{id}/games")
    public Result<Void> addGameToCollection(HttpServletRequest request, @PathVariable Long id,
                                            @RequestBody Map<String, Long> body) {
        Long userId = (Long) request.getAttribute("userId");
        Long gameId = body.get("gameId");
        if (gameId == null) {
            return Result.error("游戏ID不能为空");
        }
        collectionService.addGameToCollection(userId, id, gameId);
        return Result.successMessage("已加入合集");
    }

    @DeleteMapping("/{id}/games/{gameId}")
    public Result<Void> removeGameFromCollection(HttpServletRequest request, @PathVariable Long id,
                                                  @PathVariable Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        collectionService.removeGameFromCollection(userId, id, gameId);
        return Result.successMessage("已从合集移除");
    }

    @PutMapping("/{id}/games/reorder")
    public Result<Void> reorderGames(HttpServletRequest request, @PathVariable Long id,
                                     @RequestBody Map<String, List<Long>> body) {
        Long userId = (Long) request.getAttribute("userId");
        List<Long> gameIds = body.get("gameIds");
        if (gameIds == null || gameIds.isEmpty()) {
            return Result.error("排序数据不能为空");
        }
        collectionService.reorderGames(userId, id, gameIds);
        return Result.successMessage("排序已更新");
    }

    @GetMapping("/public")
    public Result<List<Collection>> getPublicCollections(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(collectionService.getPublicCollections(page, size));
    }

    @GetMapping("/game-collections/{gameId}")
    public Result<List<Long>> getGameCollectionIds(HttpServletRequest request, @PathVariable Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(collectionService.getGameCollectionIds(userId, gameId));
    }

    @GetMapping("/count")
    public Result<Integer> getMyCollectionCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(collectionService.countByUserId(userId));
    }
}
