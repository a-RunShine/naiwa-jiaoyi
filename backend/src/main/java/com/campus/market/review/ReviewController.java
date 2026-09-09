package com.campus.market.review;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.review.dto.ReviewCreateRequest;
import com.campus.market.review.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "评价")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/api/orders/{orderId}/review")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "提交评价")
    public Result<ReviewVO> create(@PathVariable Long orderId, @RequestBody @Valid ReviewCreateRequest req) {
        return Result.ok(reviewService.create(UserContext.require(), orderId, req));
    }

    @GetMapping("/api/orders/{orderId}/reviews")
    @Operation(summary = "订单的所有评价")
    public Result<PageResult<ReviewVO>> listByOrder(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(reviewService.listByOrder(orderId, page, size));
    }

    @GetMapping("/api/users/{userId}/reviews")
    @Operation(summary = "用户收到的评价")
    public Result<PageResult<ReviewVO>> receivedByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(reviewService.receivedByUser(userId, page, size));
    }
}