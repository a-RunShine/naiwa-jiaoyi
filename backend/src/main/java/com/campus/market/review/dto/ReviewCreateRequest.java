package com.campus.market.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 500, message = "评价 ≤500 字符") String content
) {
}