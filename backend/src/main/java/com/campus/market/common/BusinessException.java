package com.campus.market.common;

/**
 * 业务异常,带错误码。由 {@link GlobalExceptionHandler} 转为 Result.fail(code, msg)。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode ec) {
        super(ec.getMsg());
        this.code = ec.getCode();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}