package com.campus.market.order.vo;

import com.campus.market.order.entity.Order;
import com.campus.market.order.enums.OrderStatus;
import com.campus.market.order.enums.OrderTradeMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图(含交易快照)。
 */
public record OrderVO(
        Long id,
        Long itemId,
        Long sellerId,
        Long buyerId,
        BigDecimal priceSnapshot,
        OrderTradeMethod tradeMethod,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt
) {
    public static OrderVO from(Order o) {
        return new OrderVO(o.getId(), o.getItemId(), o.getSellerId(), o.getBuyerId(),
                o.getPriceSnapshot(), o.getTradeMethod(), o.getStatus(),
                o.getCreatedAt(), o.getUpdatedAt(), o.getCompletedAt());
    }
}