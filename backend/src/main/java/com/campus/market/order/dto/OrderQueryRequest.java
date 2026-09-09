package com.campus.market.order.dto;

import com.campus.market.order.enums.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 订单列表查询。{@code role} 取 buy/sell 必填。
 * 同样改用 class 让校验注解直接生效(MyBatis/Validation 走反射 getter)。
 */
public class OrderQueryRequest {

    @NotBlank
    private String role;
    private OrderStatus status;

    @Min(value = 1, message = "page ≥ 1")
    private int page;

    @Min(value = 1)
    @Max(value = 50)
    private int size;

    public OrderQueryRequest() {
    }

    public OrderQueryRequest(String role, OrderStatus status, int page, int size) {
        this.role = role;
        this.status = status;
        this.page = page;
        this.size = size;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}