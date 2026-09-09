package com.campus.market.discussion.vo;

import com.campus.market.discussion.entity.OrderMessage;
import com.campus.market.user.dto.UserVO;

import java.time.LocalDateTime;

public record OrderMessageVO(
        Long id,
        Long orderId,
        Long senderId,
        String content,
        LocalDateTime createdAt,
        String senderNickname,
        String senderAvatarUrl
) {
    public static OrderMessageVO from(OrderMessage m, UserVO sender) {
        return new OrderMessageVO(m.getId(), m.getOrderId(), m.getSenderId(),
                m.getContent(), m.getCreatedAt(),
                sender == null ? null : sender.nickname(),
                sender == null ? null : sender.avatarUrl());
    }
}