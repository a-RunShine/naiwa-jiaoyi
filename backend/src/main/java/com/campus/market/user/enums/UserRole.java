package com.campus.market.user.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 用户角色。MP 通过 IEnum 自动处理 entity 字段与 DB VARCHAR 互转。
 */
public enum UserRole implements IEnum<String> {
    USER,
    ADMIN;

    @Override
    public String getValue() {
        return name();
    }

    public static UserRole of(String value) {
        return value == null ? null : UserRole.valueOf(value);
    }
}