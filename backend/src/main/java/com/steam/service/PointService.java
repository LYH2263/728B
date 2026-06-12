package com.steam.service;

import com.steam.dto.ExchangeResultDTO;
import com.steam.dto.PageResult;
import com.steam.dto.SignInResultDTO;
import com.steam.entity.*;
import com.steam.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointsMapper userPointsMapper;
    private final PointLogMapper pointLogMapper;
    private final PointProductMapper pointProductMapper;
    private final ExchangeRecordMapper exchangeRecordMapper;
    private final UserMapper userMapper;

    private static final int BASE_SIGN_IN_POINTS = 10;
    private static final int CONSECUTIVE_BONUS_THRESHOLD = 7;
    private static final int CONSECUTIVE_BONUS_POINTS = 50;

    @Transactional
    public SignInResultDTO signIn(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserPoints points = getOrCreateUserPoints(userId);
        LocalDate today = LocalDate.now();

        SignInResultDTO result = new SignInResultDTO();

        if (points.getLastSignDate() != null && points.getLastSignDate().isEqual(today)) {
            result.setSignedToday(true);
            result.setConsecutiveDays(points.getConsecutiveDays());
            result.setPointsEarned(0);
            result.setBonusPoints(0);
            result.setTotalPoints(points.getBalance());
            result.setMessage("今日已签到");
            return result;
        }

        int consecutiveDays = calculateConsecutiveDays(points.getLastSignDate(), points.getConsecutiveDays());
        int basePoints = BASE_SIGN_IN_POINTS;
        int bonusPoints = 0;

        if (consecutiveDays > 0 && consecutiveDays % CONSECUTIVE_BONUS_THRESHOLD == 0) {
            bonusPoints = CONSECUTIVE_BONUS_POINTS;
        }

        int totalEarned = basePoints + bonusPoints;

        userPointsMapper.addPoints(userId, totalEarned);
        userPointsMapper.updateSignInfo(userId, consecutiveDays, today);

        UserPoints updatedPoints = userPointsMapper.findByUserId(userId);

        PointLog pointLog = new PointLog();
        pointLog.setUserId(userId);
        pointLog.setType("EARN");
        pointLog.setAmount(totalEarned);
        pointLog.setBalanceAfter(updatedPoints.getBalance());
        pointLog.setSource("SIGN_IN");
        pointLog.setSourceId(today.toString());
        pointLog.setDescription(buildSignInDescription(consecutiveDays, basePoints, bonusPoints));
        pointLogMapper.insert(pointLog);

        result.setSignedToday(true);
        result.setConsecutiveDays(consecutiveDays);
        result.setPointsEarned(basePoints);
        result.setBonusPoints(bonusPoints);
        result.setTotalPoints(updatedPoints.getBalance());
        result.setMessage(buildSignInMessage(consecutiveDays, totalEarned, bonusPoints));

        log.info("用户签到成功: userId={}, consecutiveDays={}, pointsEarned={}", userId, consecutiveDays, totalEarned);
        return result;
    }

    public SignInResultDTO getSignInStatus(Long userId) {
        UserPoints points = getOrCreateUserPoints(userId);
        LocalDate today = LocalDate.now();

        SignInResultDTO result = new SignInResultDTO();
        result.setSignedToday(points.getLastSignDate() != null && points.getLastSignDate().isEqual(today));
        result.setConsecutiveDays(points.getConsecutiveDays());
        result.setPointsEarned(0);
        result.setBonusPoints(0);
        result.setTotalPoints(points.getBalance());
        result.setMessage(result.getSignedToday() ? "今日已签到" : "今日未签到");
        return result;
    }

    @Transactional
    public ExchangeResultDTO exchangeProduct(Long userId, Long productId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        PointProduct product = pointProductMapper.findById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }
        if (product.getStock() <= 0) {
            throw new RuntimeException("商品库存不足");
        }

        UserPoints points = getOrCreateUserPoints(userId);
        if (points.getBalance() < product.getPointsRequired()) {
            throw new RuntimeException("积分不足");
        }

        int stockRows = pointProductMapper.decreaseStock(productId);
        if (stockRows == 0) {
            throw new RuntimeException("商品库存不足");
        }

        int pointRows = userPointsMapper.deductPoints(userId, product.getPointsRequired());
        if (pointRows == 0) {
            pointProductMapper.increaseStock(productId);
            throw new RuntimeException("积分不足");
        }

        UserPoints updatedPoints = userPointsMapper.findByUserId(userId);

        String redeemCode = null;
        if ("COUPON".equals(product.getType())) {
            redeemCode = generateRedeemCode();
        }

        ExchangeRecord record = new ExchangeRecord();
        record.setUserId(userId);
        record.setProductId(productId);
        record.setProductName(product.getName());
        record.setProductImage(product.getImage());
        record.setPointsSpent(product.getPointsRequired());
        record.setStatus("COMPLETED");
        record.setRedeemCode(redeemCode);
        exchangeRecordMapper.insert(record);

        PointLog pointLog = new PointLog();
        pointLog.setUserId(userId);
        pointLog.setType("SPEND");
        pointLog.setAmount(product.getPointsRequired());
        pointLog.setBalanceAfter(updatedPoints.getBalance());
        pointLog.setSource("EXCHANGE");
        pointLog.setSourceId(String.valueOf(record.getId()));
        pointLog.setDescription("兑换商品: " + product.getName());
        pointLogMapper.insert(pointLog);

        ExchangeResultDTO result = new ExchangeResultDTO();
        result.setRecordId(record.getId());
        result.setProductName(product.getName());
        result.setProductImage(product.getImage());
        result.setPointsSpent(product.getPointsRequired());
        result.setRemainingPoints(updatedPoints.getBalance());
        result.setRedeemCode(redeemCode);
        result.setMessage("兑换成功");

        log.info("积分兑换成功: userId={}, productId={}, productName={}, pointsSpent={}",
                userId, productId, product.getName(), product.getPointsRequired());
        return result;
    }

    @Transactional
    public void addPoints(Long userId, int amount, String source, String sourceId, String description) {
        if (amount <= 0) {
            return;
        }

        PointLog existingLog = pointLogMapper.findByUserAndSource(userId, source, sourceId);
        if (existingLog != null) {
            log.warn("积分已发放，跳过重复发放: userId={}, source={}, sourceId={}", userId, source, sourceId);
            return;
        }

        getOrCreateUserPoints(userId);
        userPointsMapper.addPoints(userId, amount);

        UserPoints updatedPoints = userPointsMapper.findByUserId(userId);

        PointLog pointLog = new PointLog();
        pointLog.setUserId(userId);
        pointLog.setType("EARN");
        pointLog.setAmount(amount);
        pointLog.setBalanceAfter(updatedPoints.getBalance());
        pointLog.setSource(source);
        pointLog.setSourceId(sourceId);
        pointLog.setDescription(description);
        pointLogMapper.insert(pointLog);

        log.info("积分发放成功: userId={}, amount={}, source={}, sourceId={}", userId, amount, source, sourceId);
    }

    public UserPoints getUserPoints(Long userId) {
        return getOrCreateUserPoints(userId);
    }

    public PageResult<PointLog> getPointLogs(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<PointLog> logs = pointLogMapper.findByUserId(userId, offset, size);
        int total = pointLogMapper.countByUserId(userId);
        return PageResult.of(logs, (long) total, page, size);
    }

    public List<PointProduct> getAllProducts() {
        return pointProductMapper.findAllActive();
    }

    public PageResult<ExchangeRecord> getExchangeRecords(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<ExchangeRecord> records = exchangeRecordMapper.findByUserId(userId, offset, size);
        int total = exchangeRecordMapper.countByUserId(userId);

        for (ExchangeRecord record : records) {
            PointProduct product = pointProductMapper.findById(record.getProductId());
            record.setProduct(product);
        }

        return PageResult.of(records, (long) total, page, size);
    }

    private UserPoints getOrCreateUserPoints(Long userId) {
        UserPoints points = userPointsMapper.findByUserId(userId);
        if (points == null) {
            UserPoints newPoints = new UserPoints();
            newPoints.setUserId(userId);
            newPoints.setBalance(0);
            newPoints.setTotalEarned(0);
            newPoints.setTotalSpent(0);
            newPoints.setConsecutiveDays(0);
            userPointsMapper.insert(newPoints);
            return userPointsMapper.findByUserId(userId);
        }
        return points;
    }

    private int calculateConsecutiveDays(LocalDate lastSignDate, int currentConsecutive) {
        if (lastSignDate == null) {
            return 1;
        }
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (lastSignDate.isEqual(yesterday)) {
            return currentConsecutive + 1;
        } else if (lastSignDate.isBefore(yesterday)) {
            return 1;
        }
        return currentConsecutive;
    }

    private String buildSignInDescription(int consecutiveDays, int basePoints, int bonusPoints) {
        StringBuilder sb = new StringBuilder();
        sb.append("每日签到 +").append(basePoints).append("积分");
        if (bonusPoints > 0) {
            sb.append("，连续").append(consecutiveDays).append("天额外奖励+").append(bonusPoints).append("积分");
        }
        return sb.toString();
    }

    private String buildSignInMessage(int consecutiveDays, int totalEarned, int bonusPoints) {
        StringBuilder sb = new StringBuilder();
        sb.append("签到成功！获得").append(totalEarned).append("积分");
        if (bonusPoints > 0) {
            sb.append("（含连续").append(consecutiveDays).append("天奖励").append(bonusPoints).append("积分）");
        }
        sb.append("，连续签到").append(consecutiveDays).append("天");
        return sb.toString();
    }

    private String generateRedeemCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "CPN-" + timestamp + "-" + uuid;
    }
}
