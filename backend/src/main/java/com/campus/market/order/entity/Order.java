package com.campus.market.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.market.order.enums.OrderStatus;
import com.campus.market.order.enums.OrderTradeMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体。表名 {@code order} 是 MySQL 关键字,@TableName 必须反引号包裹。
 */
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;
    private Long sellerId;
    private Long buyerId;
    private BigDecimal priceSnapshot;
    private OrderTradeMethod tradeMethod;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public BigDecimal getPriceSnapshot() { return priceSnapshot; }
    public void setPriceSnapshot(BigDecimal priceSnapshot) { this.priceSnapshot = priceSnapshot; }
    public OrderTradeMethod getTradeMethod() { return tradeMethod; }
    public void setTradeMethod(OrderTradeMethod tradeMethod) { this.tradeMethod = tradeMethod; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}