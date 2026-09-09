package com.campus.market.wallet.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 钱包流水类型。
 *
 * <pre>
 * 符号约定(配合 plan.md §WalletFlow):
 *   RECHARGE       balance +amount  (可用余额增)
 *   FREEZE         frozen  +amount  (冻结增)
 *   UNFREEZE       frozen  -amount  (冻结减)
 *   SETTLEMENT_OUT balance -amount, frozen -amount  (买家确认收货:扣减)
 *   SETTLEMENT_IN  balance +amount  (卖家入账)
 * </pre>
 */
public enum FlowType implements IEnum<String> {
    RECHARGE,
    FREEZE,
    UNFREEZE,
    SETTLEMENT_OUT,
    SETTLEMENT_IN;

    @Override
    public String getValue() {
        return name();
    }

    public static FlowType of(String value) {
        return value == null ? null : FlowType.valueOf(value);
    }
}