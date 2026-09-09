package com.campus.market.wallet.vo;

import java.math.BigDecimal;

/**
 * 钱包视图:{balance, frozen, available},available = balance - frozen。
 */
public record WalletVO(BigDecimal balance, BigDecimal frozen, BigDecimal available) {
}