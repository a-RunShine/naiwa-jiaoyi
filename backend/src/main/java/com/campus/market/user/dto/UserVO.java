package com.campus.market.user.dto;

import com.campus.market.user.entity.User;
import com.campus.market.user.enums.UserRole;

import java.time.LocalDateTime;

/**
 * 用户视图对象。
 */
public record UserVO(Long id, String nickname, String avatarUrl, UserRole role, LocalDateTime createdAt) {

    public static UserVO from(User u) {
        return new UserVO(u.getId(), u.getNickname(), u.getAvatarUrl(), u.getRole(), u.getCreatedAt());
    }
}