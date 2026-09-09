package com.campus.market.common;

/**
 * 业务错误码(对齐 plan.md §模块设计 / 错误码表)。
 * HTTP 状态码全部 200,前端按 {@code code} 判定。
 */
public enum ErrorCode {
    UNAUTHORIZED(1001, "未登录"),
    FORBIDDEN(1002, "无权限"),
    ITEM_NOT_FOUND(2001, "商品不存在"),
    ITEM_ALREADY_RESERVED(2002, "已被预订"),
    TRADE_METHOD_MISMATCH(2003, "交易方式不匹配"),
    INSUFFICIENT_BALANCE(3001, "余额不足"),
    INSUFFICIENT_FROZEN(3002, "冻结不足"),
    ORDER_STATUS_INVALID(4001, "订单状态非法"),
    REVIEW_ALREADY_EXISTS(5001, "已评价");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}