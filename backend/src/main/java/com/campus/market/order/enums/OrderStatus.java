package com.campus.market.order.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 订单状态(对齐 plan.md §核心数据结构 Order)。
 */
public enum OrderStatus implements IEnum<String> {
    PENDING,
    REJECTED,
    PENDING_HANDOVER,
    COMPLETED,
    CANCELLED;

    @Override
    public String getValue() {
        return name();
    }

    public static OrderStatus of(String value) {
        return value == null ? null : OrderStatus.valueOf(value);
    }
}