package com.campus.market.discussion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank @Size(min = 1, max = 500, message = "留言 1-500 字符") String content,
        Long parentId
) {
}