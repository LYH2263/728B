package com.steam.controller;

import com.steam.dto.FlashSaleOrderDTO;
import com.steam.dto.Result;
import com.steam.entity.FlashSale;
import com.steam.entity.Order;
import com.steam.service.FlashSaleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flash-sales")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @GetMapping
    public Result<List<FlashSale>> getFlashSaleList() {
        List<FlashSale> list = flashSaleService.getFlashSaleList();
        return Result.success(list);
    }

    @GetMapping("/{flashSaleId}/purchase-status")
    public Result<Map<String, Object>> getPurchaseStatus(HttpServletRequest request,
                                                          @PathVariable Long flashSaleId) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> data = new HashMap<>();
        if (userId != null) {
            int purchased = flashSaleService.getUserPurchasedCount(flashSaleId, userId);
            data.put("purchasedCount", purchased);
            data.put("isLoggedIn", true);
        } else {
            data.put("purchasedCount", 0);
            data.put("isLoggedIn", false);
        }
        return Result.success(data);
    }

    @PostMapping("/order")
    public Result<Order> createFlashSaleOrder(HttpServletRequest request,
                                               @Valid @RequestBody FlashSaleOrderDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Order order = flashSaleService.createFlashSaleOrder(userId, dto.getFlashSaleId());
        return Result.success("秒杀下单成功", order);
    }
}
