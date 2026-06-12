package com.steam.service;

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
public class FlashSaleService {

    private final FlashSaleMapper flashSaleMapper;
    private final GameMapper gameMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final UserLibraryMapper userLibraryMapper;
    private final CartMapper cartMapper;
    private final WishlistMapper wishlistMapper;

    public static final String STATUS_NOT_STARTED = "NOT_STARTED";
    public static final String STATUS_ONGOING = "ONGOING";
    public static final String STATUS_SOLD_OUT = "SOLD_OUT";
    public static final String STATUS_ENDED = "ENDED";

    public List<FlashSale> getFlashSaleList() {
        List<FlashSale> list = flashSaleMapper.findAllWithGames();
        LocalDateTime now = LocalDateTime.now();
        for (FlashSale fs : list) {
            fs.setActivityStatus(calculateStatus(fs, now));
        }
        return list;
    }

    private String calculateStatus(FlashSale fs, LocalDateTime now) {
        if (fs.getStockCount() <= 0 || (fs.getSoldCount() != null && fs.getSoldCount() >= fs.getStockCount())) {
            return STATUS_SOLD_OUT;
        }
        if (now.isBefore(fs.getStartTime())) {
            return STATUS_NOT_STARTED;
        }
        if (now.isAfter(fs.getEndTime())) {
            return STATUS_ENDED;
        }
        return STATUS_ONGOING;
    }

    @Transactional
    public Order createFlashSaleOrder(Long userId, Long flashSaleId) {
        FlashSale flashSale = flashSaleMapper.findById(flashSaleId);
        if (flashSale == null) {
            throw new RuntimeException("秒杀活动不存在");
        }
        if (flashSale.getStatus() != 1) {
            throw new RuntimeException("秒杀活动已关闭");
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(flashSale.getStartTime())) {
            throw new RuntimeException("秒杀活动尚未开始");
        }
        if (now.isAfter(flashSale.getEndTime())) {
            throw new RuntimeException("秒杀活动已结束");
        }

        int purchased = flashSaleMapper.countUserPurchased(flashSaleId, userId);
        if (purchased >= flashSale.getPerUserLimit()) {
            throw new RuntimeException("您已达到限购数量（每人限" + flashSale.getPerUserLimit() + "份）");
        }

        Game game = gameMapper.findById(flashSale.getGameId());
        if (game == null) {
            throw new RuntimeException("游戏已下架");
        }

        if (userLibraryMapper.existsByUserIdAndGameId(userId, game.getId())) {
            throw new RuntimeException("您已拥有该游戏: " + game.getTitle());
        }

        int rows = flashSaleMapper.decreaseStock(flashSaleId);
        if (rows == 0) {
            throw new RuntimeException("手慢了，秒杀商品已被抢光！");
        }

        FlashSaleOrder fsOrder = new FlashSaleOrder();
        fsOrder.setFlashSaleId(flashSaleId);
        fsOrder.setUserId(userId);
        fsOrder.setGameId(game.getId());
        fsOrder.setQuantity(1);
        fsOrder.setPrice(flashSale.getFlashPrice());
        fsOrder.setStatus("PENDING");
        flashSaleMapper.insertFlashSaleOrder(fsOrder);

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        BigDecimal payAmount = flashSale.getFlashPrice();
        if (user.getBalance().compareTo(payAmount) < 0) {
            throw new RuntimeException("余额不足，请先充值");
        }

        userMapper.updateBalance(userId, user.getBalance().subtract(payAmount));

        String orderNo = generateOrderNo();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setTotalAmount(game.getOriginalPrice());
        order.setPayAmount(payAmount);
        order.setDiscountAmount(game.getOriginalPrice().subtract(payAmount));
        orderMapper.insert(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setGameId(game.getId());
        orderItem.setGameTitle(game.getTitle());
        orderItem.setGameCover(game.getCoverImage());
        orderItem.setPrice(payAmount);
        orderItem.setQuantity(1);
        orderMapper.insertOrderItem(orderItem);

        UserLibrary library = new UserLibrary();
        library.setUserId(userId);
        library.setGameId(game.getId());
        library.setOrderId(order.getId());
        userLibraryMapper.insert(library);

        cartMapper.deleteByUserIdAndGameId(userId, game.getId());
        wishlistMapper.deleteByUserIdAndGameId(userId, game.getId());

        LocalDateTime payTime = LocalDateTime.now();
        orderMapper.updateStatus(order.getId(), "PAID", payTime);
        order.setStatus("PAID");
        order.setPayTime(payTime);

        flashSaleMapper.updateOrderPaid(fsOrder.getId(), order.getId());

        log.info("秒杀下单成功: flashSaleId={}, userId={}, orderNo={}, 秒杀价={}",
                flashSaleId, userId, orderNo, payAmount);
        return order;
    }

    public int getUserPurchasedCount(Long flashSaleId, Long userId) {
        return flashSaleMapper.countUserPurchased(flashSaleId, userId);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "FS" + timestamp + uuid;
    }
}
