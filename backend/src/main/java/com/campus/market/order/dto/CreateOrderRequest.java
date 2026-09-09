package com.campus.market.order.dto;

import com.campus.market.order.enums.OrderTradeMethod;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull Long itemId,
        @NotNull OrderTradeMethod tradeMethod
) {
}