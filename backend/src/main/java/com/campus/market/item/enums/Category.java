package com.campus.market.item.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 商品分类(对齐 CONTEXT.md 术语表 + tech-stack.md §五)。
 */
public enum Category implements IEnum<String> {
    DIGITAL("数码"),
    CLOTHING("服饰"),
    BEAUTY("美妆"),
    BOOKS("书籍"),
    DAILY("生活用品"),
    SPORTS("运动"),
    OTHER("其他");

    private final String display;

    Category(String display) {
        this.display = display;
    }

    @Override
    public String getValue() {
        return name();
    }

    public String getDisplay() {
        return display;
    }

    public static Category of(String value) {
        return value == null ? null : Category.valueOf(value);
    }
}