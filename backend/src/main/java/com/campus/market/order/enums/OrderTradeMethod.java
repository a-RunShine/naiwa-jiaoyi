package com.campus.market.order.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 订单实际交易方式(下单时确定)。对应 Item.tradeMethod 范围。
 */
public enum OrderTradeMethod implements IEnum<String> {
    OFFLINE,
    ESCROW;

    @Override
    public String getValue() {
        return name();
    }

    public static OrderTradeMethod of(String value) {
        return value == null ? null : OrderTradeMethod.valueOf(value);
    }
}