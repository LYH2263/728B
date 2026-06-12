package com.steam.controller;

import com.steam.dto.ExchangeResultDTO;
import com.steam.dto.PageResult;
import com.steam.dto.Result;
import com.steam.dto.SignInResultDTO;
import com.steam.entity.ExchangeRecord;
import com.steam.entity.PointLog;
import com.steam.entity.PointProduct;
import com.steam.entity.UserPoints;
import com.steam.service.PointService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/info")
    public Result<UserPoints> getPointInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserPoints points = pointService.getUserPoints(userId);
        return Result.success(points);
    }

    @PostMapping("/sign-in")
    public Result<SignInResultDTO> signIn(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SignInResultDTO result = pointService.signIn(userId);
        return Result.success(result.getMessage(), result);
    }

    @GetMapping("/sign-in/status")
    public Result<SignInResultDTO> getSignInStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SignInResultDTO result = pointService.getSignInStatus(userId);
        return Result.success(result);
    }

    @GetMapping("/products")
    public Result<List<PointProduct>> getProducts() {
        List<PointProduct> products = pointService.getAllProducts();
        return Result.success(products);
    }

    @PostMapping("/exchange/{productId}")
    public Result<ExchangeResultDTO> exchangeProduct(HttpServletRequest request, @PathVariable Long productId) {
        Long userId = (Long) request.getAttribute("userId");
        ExchangeResultDTO result = pointService.exchangeProduct(userId, productId);
        return Result.success(result.getMessage(), result);
    }

    @GetMapping("/logs")
    public Result<PageResult<PointLog>> getPointLogs(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<PointLog> result = pointService.getPointLogs(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/exchange-records")
    public Result<PageResult<ExchangeRecord>> getExchangeRecords(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<ExchangeRecord> result = pointService.getExchangeRecords(userId, page, size);
        return Result.success(result);
    }
}
