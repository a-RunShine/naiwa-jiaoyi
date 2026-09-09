package com.campus.market.item.dto;

import com.campus.market.item.enums.Category;
import com.campus.market.item.enums.ItemCondition;
import com.campus.market.item.enums.ItemStatus;
import com.campus.market.item.enums.TradeMethod;

import java.math.BigDecimal;

/**
 * 商品列表查询参数(query string 绑定)。MyBatis OGNL 不支持 record 属性访问,
 * 改用传统 class + getter,避免 {@code q.status} 之类的 OGNL 表达式报错。
 */
public class ItemQueryRequest {

    private ItemStatus status;
    private Category category;
    private ItemCondition condition;
    private TradeMethod tradeMethod;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String keyword;
    private String sort;
    private String sellerId;
    private Integer page;
    private Integer size;

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public ItemCondition getCondition() { return condition; }
    public void setCondition(ItemCondition condition) { this.condition = condition; }

    public TradeMethod getTradeMethod() { return tradeMethod; }
    public void setTradeMethod(TradeMethod tradeMethod) { this.tradeMethod = tradeMethod; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public int pageOrDefault() {
        int p = page == null ? 1 : page;
        return Math.max(1, p);
    }

    public int sizeOrDefault() {
        int s = size == null ? 20 : size;
        if (s < 1) s = 1;
        if (s > 50) s = 50;
        return s;
    }
}