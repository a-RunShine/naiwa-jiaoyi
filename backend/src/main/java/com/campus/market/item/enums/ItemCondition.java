package com.campus.market.item.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 商品成色。
 */
public enum ItemCondition implements IEnum<String> {
    NEW("全新"),
    NEARLY_NEW("95新"),
    VERY_GOOD("9成新"),
    NEEDS_REPAIR("需维修");

    private final String display;

    ItemCondition(String display) {
        this.display = display;
    }

    @Override
    public String getValue() {
        return name();
    }

    public String getDisplay() {
        return display;
    }

    public static ItemCondition of(String value) {
        return value == null ? null : ItemCondition.valueOf(value);
    }
}