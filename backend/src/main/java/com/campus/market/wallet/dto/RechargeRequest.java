package com.campus.market.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 钱包充值请求。MVP 阶段金额任意 ≥0.01 都接受(模拟)。
 */
public record RechargeRequest(
        @NotNull @DecimalMin(value = "0.01", message = "金额必须 > 0") @Digits(integer = 8, fraction = 2, message = "金额最多 8 位整数 + 2 位小数")
        BigDecimal amount
) {
}