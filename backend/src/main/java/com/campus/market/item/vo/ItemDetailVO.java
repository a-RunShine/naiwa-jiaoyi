package com.campus.market.item.vo;

import com.campus.market.item.entity.Item;
import com.campus.market.item.enums.Category;
import com.campus.market.item.enums.ItemCondition;
import com.campus.market.item.enums.ItemStatus;
import com.campus.market.item.enums.TradeMethod;
import com.campus.market.user.dto.UserVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情视图:item 字段 + 卖家 UserVO。images 字段已反序列化为 List。
 */
public record ItemDetailVO(
        Long id,
        Long sellerId,
        String title,
        String description,
        Category category,
        ItemCondition condition,
        BigDecimal price,
        TradeMethod tradeMethod,
        ItemStatus status,
        List<String> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserVO seller
) {

    public static ItemDetailVO of(Item it, UserVO seller, List<String> images) {
        return new ItemDetailVO(
                it.getId(),
                it.getSellerId(),
                it.getTitle(),
                it.getDescription(),
                it.getCategory(),
                it.getCondition(),
                it.getPrice(),
                it.getTradeMethod(),
                it.getStatus(),
                images,
                it.getCreatedAt(),
                it.getUpdatedAt(),
                seller
        );
    }
}