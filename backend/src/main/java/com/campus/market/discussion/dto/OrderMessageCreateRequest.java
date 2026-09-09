package com.campus.market.discussion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderMessageCreateRequest(
        @NotBlank @Size(min = 1, max = 500, message = "消息 1-500 字符") String content
) {
}