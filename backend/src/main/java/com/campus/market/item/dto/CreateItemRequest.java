package com.campus.market.item.dto;

import com.campus.market.item.enums.Category;
import com.campus.market.item.enums.ItemCondition;
import com.campus.market.item.enums.TradeMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布商品请求体。
 */
public record CreateItemRequest(
        @NotBlank @Size(min = 5, max = 60, message = "标题 5-60 字符")
        String title,

        @NotBlank @Size(min = 10, max = 2000, message = "描述 10-2000 字符")
        String description,

        @NotNull(message = "分类不能为空")
        Category category,

        @NotNull(message = "成色不能为空")
        ItemCondition condition,

        @NotNull @DecimalMin(value = "0.01", message = "价格必须 > 0")
        BigDecimal price,

        @NotNull(message = "交易方式不能为空")
        TradeMethod tradeMethod,

        @NotEmpty(message = "至少 1 张图片")
        List<@Size(max = 1024, message = "图片 URL ≤1024") String> images
) {
}