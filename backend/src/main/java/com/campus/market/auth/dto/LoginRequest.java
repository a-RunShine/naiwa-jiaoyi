package com.campus.market.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * 登录请求 body。
 *
 * @param code       微信 wx.login() 返回的 code;Mock 模式可空字符串
 * @param mockUserId Mock 模式登录的用户 id(1=小白,2=小红,3=小黑);非 Mock 模式可空
 */
public record LoginRequest(
        @Size(max = 64, message = "code 长度 ≤ 64")
        String code,
        Long mockUserId) {
}