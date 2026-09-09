package com.campus.market.item.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 商品状态。
 */
public enum ItemStatus implements IEnum<String> {
    ON_SALE,
    RESERVED,
    SOLD,
    OFF_SHELF;

    @Override
    public String getValue() {
        return name();
    }

    public static ItemStatus of(String value) {
        return value == null ? null : ItemStatus.valueOf(value);
    }
}