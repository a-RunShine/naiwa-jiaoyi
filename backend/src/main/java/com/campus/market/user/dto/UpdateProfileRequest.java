package com.campus.market.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新个人资料请求体。任意字段为空则不改。
 */
public record UpdateProfileRequest(
        @Size(min = 1, max = 32, message = "昵称长度 1-32")
        String nickname,

        @Pattern(regexp = "^https?://.+", message = "头像 URL 必须以 http(s):// 开头")
        @Size(max = 512, message = "头像 URL 长度 ≤512")
        String avatarUrl
) {
}