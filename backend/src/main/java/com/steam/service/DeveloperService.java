package com.steam.service;

import com.steam.entity.Developer;
import com.steam.entity.DeveloperFollow;
import com.steam.entity.Game;
import com.steam.mapper.DeveloperFollowMapper;
import com.steam.mapper.DeveloperMapper;
import com.steam.mapper.GameMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeveloperService {

    private final DeveloperMapper developerMapper;
    private final DeveloperFollowMapper developerFollowMapper;
    private final GameMapper gameMapper;
    private final NotificationService notificationService;

    public Developer getDeveloperById(Long id) {
        Developer developer = developerMapper.findById(id);
        if (developer == null) {
            throw new RuntimeException("开发商不存在");
        }
        return developer;
    }

    public List<Developer> getAllDevelopers() {
        return developerMapper.findAll();
    }

    public List<Developer> getTopDevelopers(Integer limit) {
        return developerMapper.findTop(limit != null ? limit : 10);
    }

    public List<Developer> searchDevelopers(String keyword, Integer limit) {
        return developerMapper.searchByName(keyword, limit != null ? limit : 20);
    }

    public Map<String, Object> getDeveloperDetail(Long id) {
        Developer developer = getDeveloperById(id);
        List<Game> games = gameMapper.findByDeveloper(developer.getName());

        int actualFollowerCount = developerFollowMapper.countFollowers(id);
        if (actualFollowerCount != developer.getFollowerCount()) {
            developerMapper.updateCounts(id, actualFollowerCount, games.size());
            developer.setFollowerCount(actualFollowerCount);
            developer.setGameCount(games.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("developer", developer);
        result.put("games", games);
        return result;
    }

    public boolean isFollowing(Long userId, Long developerId) {
        return developerFollowMapper.existsByUserIdAndDeveloperId(userId, developerId);
    }

    @Transactional
    public void followDeveloper(Long userId, Long developerId) {
        Developer developer = getDeveloperById(developerId);

        if (developerFollowMapper.existsByUserIdAndDeveloperId(userId, developerId)) {
            throw new RuntimeException("已关注该开发商");
        }

        DeveloperFollow follow = new DeveloperFollow();
        follow.setUserId(userId);
        follow.setDeveloperId(developerId);
        developerFollowMapper.insert(follow);

        developerMapper.incrementFollowerCount(developerId);
        log.info("用户 {} 关注了开发商 {}", userId, developer.getName());
    }

    @Transactional
    public void unfollowDeveloper(Long userId, Long developerId) {
        int rows = developerFollowMapper.deleteByUserIdAndDeveloperId(userId, developerId);
        if (rows == 0) {
            throw new RuntimeException("未关注该开发商");
        }

        developerMapper.decrementFollowerCount(developerId);
        log.info("用户 {} 取关了开发商 {}", userId, developerId);
    }

    public List<DeveloperFollow> getFollowedDevelopers(Long userId) {
        return developerFollowMapper.findByUserId(userId);
    }

    public int getFollowedCount(Long userId) {
        return developerFollowMapper.countByUserId(userId);
    }

    @Transactional
    public void notifyFollowersOfNewGame(Long gameId) {
        Game game = gameMapper.findById(gameId);
        if (game == null) return;

        String developerName = game.getDeveloper();
        if (developerName == null || developerName.isEmpty()) return;

        Developer developer = developerMapper.findByName(developerName);
        if (developer == null) return;

        List<Long> followerIds = developerFollowMapper.findFollowerUserIds(developer.getId());
        if (followerIds.isEmpty()) return;

        String type = "NEW_GAME";
        String title = developer.getName() + " 发布了新游戏！";
        String content = String.format("%s 发布了新游戏《%s》，快来看看吧！", developer.getName(), game.getTitle());

        for (Long userId : followerIds) {
            notificationService.createNewGameNotification(userId, gameId, title, content, developer.getId());
        }

        log.info("开发商 {} 发布新游戏 {}，已通知 {} 位粉丝", developer.getName(), game.getTitle(), followerIds.size());
    }

    public Developer findOrCreateByName(String name) {
        Developer developer = developerMapper.findByName(name);
        if (developer == null) {
            developer = new Developer();
            developer.setName(name);
            developer.setFollowerCount(0);
            developer.setGameCount(0);
            developerMapper.insert(developer);
            log.info("自动创建开发商实体: {}", name);
        }
        return developer;
    }
}
