package com.campus.market.discussion.vo;

import com.campus.market.discussion.entity.Comment;
import com.campus.market.user.dto.UserVO;

import java.time.LocalDateTime;

public record CommentVO(
        Long id,
        Long itemId,
        Long userId,
        Long parentId,
        String content,
        LocalDateTime createdAt,
        String authorNickname,
        String authorAvatarUrl
) {
    public static CommentVO from(Comment c, UserVO author) {
        return new CommentVO(c.getId(), c.getItemId(), c.getUserId(), c.getParentId(),
                c.getContent(), c.getCreatedAt(),
                author == null ? null : author.nickname(),
                author == null ? null : author.avatarUrl());
    }
}