package com.campus.market.review.vo;

import com.campus.market.review.entity.Review;

import java.time.LocalDateTime;

public record ReviewVO(
        Long id,
        Long orderId,
        Long authorId,
        Long targetUserId,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        String authorNickname
) {
    public static ReviewVO from(Review r, String authorNickname) {
        return new ReviewVO(r.getId(), r.getOrderId(), r.getAuthorId(), r.getTargetUserId(),
                r.getRating(), r.getContent(), r.getCreatedAt(), authorNickname);
    }
}