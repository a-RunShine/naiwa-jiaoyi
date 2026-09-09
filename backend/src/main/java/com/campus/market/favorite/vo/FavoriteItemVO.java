package com.campus.market.favorite.vo;

import com.campus.market.item.entity.Item;
import com.campus.market.item.enums.Category;
import com.campus.market.item.enums.ItemCondition;
import com.campus.market.item.enums.ItemStatus;
import com.campus.market.item.enums.TradeMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏列表项:商品概要 + 失效标志(invalid = status != ON_SALE)。
 */
public record FavoriteItemVO(
        Long id,
        Long sellerId,
        String title,
        BigDecimal price,
        Category category,
        ItemCondition condition,
        TradeMethod tradeMethod,
        ItemStatus status,
        boolean invalid,
        LocalDateTime createdAt
) {
    public static FavoriteItemVO from(Item it) {
        return new FavoriteItemVO(it.getId(), it.getSellerId(), it.getTitle(), it.getPrice(),
                it.getCategory(), it.getCondition(), it.getTradeMethod(), it.getStatus(),
                it.getStatus() != ItemStatus.ON_SALE, it.getCreatedAt());
    }
}