package com.campus.market.order;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.order.dto.CreateOrderRequest;
import com.campus.market.order.dto.OrderQueryRequest;
import com.campus.market.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "下单")
    public Result<OrderVO> create(@RequestBody @Valid CreateOrderRequest req) {
        return Result.ok(orderService.create(UserContext.require(), req));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "卖家确认")
    public Result<OrderVO> confirm(@PathVariable Long id) {
        return Result.ok(orderService.confirm(UserContext.require(), id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "卖家拒绝")
    public Result<OrderVO> reject(@PathVariable Long id) {
        return Result.ok(orderService.reject(UserContext.require(), id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "买卖双方取消")
    public Result<OrderVO> cancel(@PathVariable Long id) {
        return Result.ok(orderService.cancel(UserContext.require(), id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "买家确认收货(幂等)")
    public Result<OrderVO> complete(@PathVariable Long id) {
        return Result.ok(orderService.complete(UserContext.require(), id));
    }

    @GetMapping
    @Operation(summary = "订单列表(buy/sell 必填)")
    public Result<PageResult<OrderVO>> list(@Valid @ModelAttribute OrderQueryRequest q) {
        return Result.ok(orderService.list(UserContext.require(), q));
    }
}