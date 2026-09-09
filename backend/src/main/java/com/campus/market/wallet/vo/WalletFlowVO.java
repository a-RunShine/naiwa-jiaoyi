package com.campus.market.wallet.vo;

import com.campus.market.wallet.entity.WalletFlow;
import com.campus.market.wallet.enums.FlowType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletFlowVO(
        Long id,
        Long orderId,
        FlowType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime createdAt
) {
    public static WalletFlowVO from(WalletFlow f) {
        return new WalletFlowVO(f.getId(), f.getOrderId(), f.getType(),
                f.getAmount(), f.getBalanceAfter(), f.getCreatedAt());
    }
}