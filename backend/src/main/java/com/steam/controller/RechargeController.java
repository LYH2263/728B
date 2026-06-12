package com.steam.controller;

import com.steam.dto.PageResult;
import com.steam.dto.Result;
import com.steam.entity.BalanceLog;
import com.steam.entity.RechargeOrder;
import com.steam.entity.RechargePlan;
import com.steam.service.RechargeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recharge")
@RequiredArgsConstructor
public class RechargeController {

    private final RechargeService rechargeService;

    @GetMapping("/plans")
    public Result<List<RechargePlan>> getPlans() {
        List<RechargePlan> plans = rechargeService.getAllPlans();
        return Result.success(plans);
    }

    @GetMapping("/plans/{id}")
    public Result<RechargePlan> getPlanById(@PathVariable Long id) {
        RechargePlan plan = rechargeService.getPlanById(id);
        if (plan == null) {
            return Result.error("套餐不存在");
        }
        return Result.success(plan);
    }

    @PostMapping("/order")
    public Result<RechargeOrder> createOrder(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        Long planId = Long.valueOf(body.get("planId").toString());
        String payMethod = body.containsKey("payMethod") ? body.get("payMethod").toString() : "WECHAT";

        RechargeOrder order = rechargeService.createOrder(userId, planId, payMethod);
        return Result.success("订单创建成功", order);
    }

    @PostMapping("/order/{orderNo}/pay")
    public Result<RechargeOrder> mockPay(@PathVariable String orderNo,
                                          @RequestBody(required = false) Map<String, Object> body) {
        String transactionId = body != null && body.containsKey("transactionId")
                ? body.get("transactionId").toString()
                : "MOCK" + System.currentTimeMillis();

        RechargeOrder order = rechargeService.payCallback(orderNo, transactionId);
        return Result.success("支付成功", order);
    }

    @GetMapping("/order/{orderNo}")
    public Result<RechargeOrder> getOrder(HttpServletRequest request, @PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        RechargeOrder order = rechargeService.getOrderByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权访问此订单");
        }
        return Result.success(order);
    }

    @GetMapping("/orders")
    public Result<PageResult<RechargeOrder>> getOrders(HttpServletRequest request,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<RechargeOrder> result = rechargeService.getOrdersByUserId(userId, page, size);
        return Result.success(result);
    }

    @PostMapping("/order/{orderNo}/cancel")
    public Result<Void> cancelOrder(HttpServletRequest request, @PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = rechargeService.cancelOrder(userId, orderNo);
        if (success) {
            return Result.successMessage("取消成功");
        }
        return Result.error("取消失败");
    }

    @GetMapping("/balance-logs")
    public Result<PageResult<BalanceLog>> getBalanceLogs(HttpServletRequest request,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<BalanceLog> result = rechargeService.getBalanceLogs(userId, page, size);
        return Result.success(result);
    }
}
