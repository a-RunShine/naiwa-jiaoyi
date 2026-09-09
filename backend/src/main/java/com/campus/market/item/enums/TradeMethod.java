package com.campus.market.item.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 商品支持交易方式。
 */
public enum TradeMethod implements IEnum<String> {
    OFFLINE_ONLY,
    ESCROW_ONLY,
    BOTH;

    @Override
    public String getValue() {
        return name();
    }

    public static TradeMethod of(String value) {
        return value == null ? null : TradeMethod.valueOf(value);
    }
}