package com.campus.market.common;

/**
 * 当前请求用户上下文(基于 ThreadLocal)。由 {@code JwtInterceptor} 写入。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId) {
        CURRENT.set(userId);
    }

    /**
     * 取当前用户 id。未登录返 null,由 Controller 决定是否抛 1001。
     */
    public static Long get() {
        return CURRENT.get();
    }

    /**
     * 取当前用户 id,未登录抛 UNAUTHORIZED。需在已鉴权路径调用。
     */
    public static Long require() {
        Long uid = CURRENT.get();
        if (uid == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return uid;
    }

    public static void clear() {
        CURRENT.remove();
    }
}