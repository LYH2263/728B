package com.steam.service;

import com.steam.entity.Collection;
import com.steam.entity.CollectionGame;
import com.steam.entity.Game;
import com.steam.mapper.CollectionGameMapper;
import com.steam.mapper.CollectionMapper;
import com.steam.mapper.GameMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionMapper collectionMapper;
    private final CollectionGameMapper collectionGameMapper;
    private final GameMapper gameMapper;

    public List<Collection> getUserCollections(Long userId) {
        return collectionMapper.findByUserId(userId);
    }

    public Collection getCollectionDetail(Long id, Long currentUserId) {
        Collection collection = collectionMapper.findById(id);
        if (collection == null) {
            throw new RuntimeException("合集不存在");
        }
        if (collection.getIsPublic() == 0 && !collection.getUserId().equals(currentUserId)) {
            throw new RuntimeException("该合集为私密合集，无权查看");
        }
        return collection;
    }

    public Collection getCollectionDetailPublic(Long id) {
        Collection collection = collectionMapper.findById(id);
        if (collection == null) {
            throw new RuntimeException("合集不存在");
        }
        if (collection.getIsPublic() == 0) {
            throw new RuntimeException("该合集为私密合集");
        }
        return collection;
    }

    @Transactional
    public Collection createCollection(Long userId, String name, String description, Integer isPublic) {
        Collection collection = new Collection();
        collection.setUserId(userId);
        collection.setName(name);
        collection.setDescription(description);
        collection.setIsPublic(isPublic != null ? isPublic : 0);
        collection.setGameCount(0);
        collection.setTotalPrice(BigDecimal.ZERO);
        collection.setSortOrder(0);
        collectionMapper.insert(collection);
        log.info("用户 {} 创建合集 {}，ID: {}", userId, name, collection.getId());
        return collection;
    }

    @Transactional
    public Collection updateCollection(Long userId, Long id, String name, String description, Integer isPublic) {
        Collection existing = collectionMapper.findById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("合集不存在或无权修改");
        }
        existing.setName(name);
        existing.setDescription(description);
        if (isPublic != null) {
            existing.setIsPublic(isPublic);
        }
        collectionMapper.update(existing);
        log.info("用户 {} 更新合集 {}", userId, id);
        return existing;
    }

    @Transactional
    public void deleteCollection(Long userId, Long id) {
        Collection existing = collectionMapper.findById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("合集不存在或无权删除");
        }
        collectionGameMapper.deleteByCollectionId(id);
        collectionMapper.deleteByIdAndUserId(id, userId);
        log.info("用户 {} 删除合集 {} 及其游戏关联", userId, id);
    }

    @Transactional
    public void addGameToCollection(Long userId, Long collectionId, Long gameId) {
        Collection collection = collectionMapper.findById(collectionId);
        if (collection == null || !collection.getUserId().equals(userId)) {
            throw new RuntimeException("合集不存在或无权操作");
        }
        Game game = gameMapper.findById(gameId);
        if (game == null) {
            throw new RuntimeException("游戏不存在");
        }
        if (collectionGameMapper.existsByCollectionIdAndGameId(collectionId, gameId)) {
            throw new RuntimeException("游戏已在该合集中");
        }
        int nextSort = collectionGameMapper.maxSortOrder(collectionId) + 1;
        CollectionGame cg = new CollectionGame();
        cg.setCollectionId(collectionId);
        cg.setGameId(gameId);
        cg.setSortOrder(nextSort);
        collectionGameMapper.insert(cg);
        refreshCollectionStats(collectionId);
        refreshCollectionCoverImages(collectionId);
        log.info("用户 {} 将游戏 {} 加入合集 {}", userId, gameId, collectionId);
    }

    @Transactional
    public void removeGameFromCollection(Long userId, Long collectionId, Long gameId) {
        Collection collection = collectionMapper.findById(collectionId);
        if (collection == null || !collection.getUserId().equals(userId)) {
            throw new RuntimeException("合集不存在或无权操作");
        }
        int rows = collectionGameMapper.deleteByCollectionIdAndGameId(collectionId, gameId);
        if (rows == 0) {
            throw new RuntimeException("该游戏不在此合集中");
        }
        refreshCollectionStats(collectionId);
        refreshCollectionCoverImages(collectionId);
        log.info("用户 {} 将游戏 {} 从合集 {} 移除", userId, gameId, collectionId);
    }

    public List<CollectionGame> getCollectionGames(Long collectionId, Long currentUserId) {
        Collection collection = collectionMapper.findById(collectionId);
        if (collection == null) {
            throw new RuntimeException("合集不存在");
        }
        if (collection.getIsPublic() == 0 && !collection.getUserId().equals(currentUserId)) {
            throw new RuntimeException("该合集为私密合集，无权查看");
        }
        return collectionGameMapper.findByCollectionId(collectionId);
    }

    public List<CollectionGame> getCollectionGamesPublic(Long collectionId) {
        Collection collection = collectionMapper.findById(collectionId);
        if (collection == null) {
            throw new RuntimeException("合集不存在");
        }
        if (collection.getIsPublic() == 0) {
            throw new RuntimeException("该合集为私密合集");
        }
        return collectionGameMapper.findByCollectionId(collectionId);
    }

    @Transactional
    public void reorderGames(Long userId, Long collectionId, List<Long> gameIds) {
        Collection collection = collectionMapper.findById(collectionId);
        if (collection == null || !collection.getUserId().equals(userId)) {
            throw new RuntimeException("合集不存在或无权操作");
        }
        for (int i = 0; i < gameIds.size(); i++) {
            collectionGameMapper.updateSortOrder(collectionId, gameIds.get(i), i + 1);
        }
        log.info("用户 {} 对合集 {} 内游戏重新排序", userId, collectionId);
    }

    public List<Collection> getPublicCollections(int page, int size) {
        int offset = (page - 1) * size;
        return collectionMapper.findPublicCollections(offset, size);
    }

    public int getPublicCollectionCount() {
        return collectionMapper.countPublicCollections();
    }

    public List<Long> getGameCollectionIds(Long userId, Long gameId) {
        List<CollectionGame> links = collectionGameMapper.findByUserIdAndGameId(userId, gameId);
        List<Long> collectionIds = new ArrayList<>();
        for (CollectionGame cg : links) {
            collectionIds.add(cg.getCollectionId());
        }
        return collectionIds;
    }

    public int countByUserId(Long userId) {
        return collectionMapper.countByUserId(userId);
    }

    private void refreshCollectionStats(Long collectionId) {
        List<CollectionGame> games = collectionGameMapper.findByCollectionId(collectionId);
        int count = games.size();
        BigDecimal total = BigDecimal.ZERO;
        for (CollectionGame cg : games) {
            if (cg.getCurrentPrice() != null) {
                total = total.add(cg.getCurrentPrice());
            }
        }
        collectionMapper.updateStats(collectionId, count, total);
    }

    private void refreshCollectionCoverImages(Long collectionId) {
        List<CollectionGame> games = collectionGameMapper.findByCollectionId(collectionId);
        List<String> covers = new ArrayList<>();
        int limit = Math.min(games.size(), 4);
        for (int i = 0; i < limit; i++) {
            Game g = games.get(i).getGame();
            if (g != null && g.getCoverImage() != null) {
                covers.add(g.getCoverImage());
            }
        }
        String coverJson = covers.isEmpty() ? null :
                covers.stream().reduce((a, b) -> "\"" + a + "\",\"" + b + "\"")
                        .map(s -> "[" + s + "]").orElse(null);
        collectionMapper.updateCoverImages(collectionId, coverJson);
    }
}
