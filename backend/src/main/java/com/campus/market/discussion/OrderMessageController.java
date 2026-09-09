package com.campus.market.discussion;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.discussion.dto.OrderMessageCreateRequest;
import com.campus.market.discussion.vo.OrderMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders/{orderId}/messages")
@Tag(name = "订单协商")
@SecurityRequirement(name = "bearerAuth")
public class OrderMessageController {

    private final OrderMessageService orderMessageService;

    public OrderMessageController(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @GetMapping
    @Operation(summary = "协商消息(支持 since 增量)")
    public Result<PageResult<OrderMessageVO>> list(
            @PathVariable Long orderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Result.ok(orderMessageService.list(UserContext.require(), orderId, since, page, size));
    }

    @PostMapping
    @Operation(summary = "发送协商消息")
    public Result<OrderMessageVO> create(
            @PathVariable Long orderId,
            @RequestBody @Valid OrderMessageCreateRequest req) {
        return Result.ok(orderMessageService.create(UserContext.require(), orderId, req.content()));
    }
}