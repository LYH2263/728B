package com.steam.service;

import com.steam.dto.PageResult;
import com.steam.entity.*;
import com.steam.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeService {

    private final RechargePlanMapper rechargePlanMapper;
    private final RechargeOrderMapper rechargeOrderMapper;
    private final BalanceLogMapper balanceLogMapper;
    private final UserMapper userMapper;

    private static final int ORDER_EXPIRE_MINUTES = 15;
    private static final String ORDER_PREFIX = "RCH";

    public List<RechargePlan> getAllPlans() {
        return rechargePlanMapper.findAllActive();
    }

    public RechargePlan getPlanById(Long planId) {
        return rechargePlanMapper.findById(planId);
    }

    @Transactional
    public RechargeOrder createOrder(Long userId, Long planId, String payMethod) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        RechargePlan plan = rechargePlanMapper.findById(planId);
        if (plan == null || plan.getStatus() != 1) {
            throw new RuntimeException("充值套餐不存在或已下架");
        }

        RechargeOrder order = new RechargeOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPlanId(planId);
        order.setPlanName(plan.getName());
        order.setAmount(plan.getAmount());
        order.setBonusAmount(plan.getBonusAmount());
        order.setTotalAmount(plan.getAmount().add(plan.getBonusAmount()));
        order.setPayMethod(payMethod != null ? payMethod : "WECHAT");
        order.setStatus("PENDING");
        order.setExpireTime(LocalDateTime.now().plusMinutes(ORDER_EXPIRE_MINUTES));
        order.setRemark(plan.getDescription());

        rechargeOrderMapper.insert(order);

        log.info("充值订单创建成功: orderNo={}, userId={}, planId={}, amount={}",
                order.getOrderNo(), userId, planId, plan.getAmount());
        return order;
    }

    @Transactional
    public RechargeOrder payCallback(String orderNo, String transactionId) {
        RechargeOrder order = rechargeOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if ("PAID".equals(order.getStatus())) {
            log.warn("订单已支付，重复回调直接返回: orderNo={}", orderNo);
            return order;
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态异常，当前状态: " + order.getStatus());
        }

        BalanceLog existingLog = balanceLogMapper.findBySourceAndId("RECHARGE", orderNo);
        if (existingLog != null) {
            log.warn("充值流水已存在，幂等校验通过，直接返回: orderNo={}", orderNo);
            return order;
        }

        int updatedRows = rechargeOrderMapper.updateStatusIfPending(order.getId(), "PAID");
        if (updatedRows == 0) {
            log.warn("订单状态已变更，并发情况下幂等处理: orderNo={}", orderNo);
            return rechargeOrderMapper.findById(order.getId());
        }

        userMapper.addBalance(order.getUserId(), order.getTotalAmount());

        User updatedUser = userMapper.findById(order.getUserId());

        BalanceLog balanceLog = new BalanceLog();
        balanceLog.setUserId(order.getUserId());
        balanceLog.setType("RECHARGE");
        balanceLog.setAmount(order.getTotalAmount());
        balanceLog.setBalanceAfter(updatedUser.getBalance());
        balanceLog.setSource("RECHARGE");
        balanceLog.setSourceId(orderNo);
        balanceLog.setDescription(buildRechargeDescription(order));
        balanceLogMapper.insert(balanceLog);

        rechargeOrderMapper.updateStatus(order.getId(), "PAID", LocalDateTime.now(), transactionId);

        log.info("充值成功: orderNo={}, userId={}, amount={}, bonusAmount={}, totalAmount={}",
                orderNo, order.getUserId(), order.getAmount(), order.getBonusAmount(), order.getTotalAmount());

        return rechargeOrderMapper.findById(order.getId());
    }

    public RechargeOrder getOrderByOrderNo(String orderNo) {
        return rechargeOrderMapper.findByOrderNo(orderNo);
    }

    public RechargeOrder getOrderById(Long id) {
        return rechargeOrderMapper.findById(id);
    }

    public PageResult<RechargeOrder> getOrdersByUserId(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<RechargeOrder> orders = rechargeOrderMapper.findByUserIdPage(userId, offset, size);
        int total = rechargeOrderMapper.countByUserId(userId);
        return PageResult.of(orders, (long) total, page, size);
    }

    public PageResult<BalanceLog> getBalanceLogs(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<BalanceLog> logs = balanceLogMapper.findByUserId(userId, offset, size);
        int total = balanceLogMapper.countByUserId(userId);
        return PageResult.of(logs, (long) total, page, size);
    }

    @Transactional
    public boolean cancelOrder(Long userId, String orderNo) {
        RechargeOrder order = rechargeOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许取消");
        }

        int rows = rechargeOrderMapper.updateStatusIfPending(order.getId(), "CANCELLED");
        if (rows > 0) {
            log.info("充值订单已取消: orderNo={}", orderNo);
            return true;
        }
        return false;
    }

    @Transactional
    public void processExpiredOrders() {
        List<RechargeOrder> expiredOrders = rechargeOrderMapper.findExpiredOrders();
        for (RechargeOrder order : expiredOrders) {
            try {
                int rows = rechargeOrderMapper.updateStatusIfPending(order.getId(), "TIMEOUT");
                if (rows > 0) {
                    log.info("充值订单超时关闭: orderNo={}", order.getOrderNo());
                }
            } catch (Exception e) {
                log.error("处理超时订单失败: orderNo={}", order.getOrderNo(), e);
            }
        }
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return ORDER_PREFIX + timestamp + uuid;
    }

    private String buildRechargeDescription(RechargeOrder order) {
        StringBuilder sb = new StringBuilder();
        sb.append("充值").append(order.getAmount()).append("元");
        if (order.getBonusAmount() != null && order.getBonusAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("，赠送").append(order.getBonusAmount()).append("元");
        }
        sb.append("，共到账").append(order.getTotalAmount()).append("元");
        return sb.toString();
    }
}
