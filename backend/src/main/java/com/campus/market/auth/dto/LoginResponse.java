package com.campus.market.auth.dto;

import com.campus.market.user.dto.UserVO;

/**
 * 登录响应:{token, user}。
 */
public record LoginResponse(String token, UserVO user) {
}