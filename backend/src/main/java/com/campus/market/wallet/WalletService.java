package com.campus.market.wallet;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.PageResult;
import com.campus.market.wallet.entity.Wallet;
import com.campus.market.wallet.entity.WalletFlow;
import com.campus.market.wallet.enums.FlowType;
import com.campus.market.wallet.mapper.WalletFlowMapper;
import com.campus.market.wallet.mapper.WalletMapper;
import com.campus.market.wallet.vo.WalletFlowVO;
import com.campus.market.wallet.vo.WalletVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 钱包服务。
 *
 * <ul>
 *   <li>{@link #recharge} 独立事务</li>
 *   <li>{@link #freeze} / {@link #unfreeze} / {@link #settle} 强制嵌套在调用方事务内(MANDATORY)</li>
 * </ul>
 */
@Service
public class WalletService {

    private final WalletMapper walletMapper;
    private final WalletFlowMapper flowMapper;

    public WalletService(WalletMapper walletMapper, WalletFlowMapper flowMapper) {
        this.walletMapper = walletMapper;
        this.flowMapper = flowMapper;
    }

    @Transactional
    public WalletVO recharge(Long uid, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(400, "金额必须 > 0");
        }
        Wallet w = walletMapper.selectByUserIdForUpdate(uid);
        if (w == null) {
            w = new Wallet();
            w.setUserId(uid);
            w.setBalance(BigDecimal.ZERO);
            w.setFrozen(BigDecimal.ZERO);
            walletMapper.insert(w);
            w = walletMapper.selectByUserIdForUpdate(uid);
        }
        walletMapper.updateBalance(uid, amount, BigDecimal.ZERO);
        Wallet after = walletMapper.selectById(uid);
        recordFlow(uid, null, FlowType.RECHARGE, amount, after.getBalance());
        return new WalletVO(after.getBalance(), after.getFrozen(),
                after.getBalance().subtract(after.getFrozen()));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void freeze(Long uid, Long orderId, BigDecimal amount) {
        Wallet w = walletMapper.selectByUserIdForUpdate(uid);
        if (w == null || w.getBalance().subtract(w.getFrozen()).compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        walletMapper.updateBalance(uid, BigDecimal.ZERO, amount);
        Wallet after = walletMapper.selectById(uid);
        recordFlow(uid, orderId, FlowType.FREEZE, amount, after.getBalance());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void unfreeze(Long uid, Long orderId, BigDecimal amount) {
        Wallet w = walletMapper.selectByUserIdForUpdate(uid);
        if (w == null || w.getFrozen().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FROZEN);
        }
        walletMapper.updateBalance(uid, BigDecimal.ZERO, amount.negate());
        Wallet after = walletMapper.selectById(uid);
        recordFlow(uid, orderId, FlowType.UNFREEZE, amount, after.getBalance());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void settle(Long buyerId, Long sellerId, Long orderId, BigDecimal amount) {
        // 买家:balance 与 frozen 同时减 amount(单条 SQL)
        Wallet bw = walletMapper.selectByUserIdForUpdate(buyerId);
        if (bw == null || bw.getFrozen().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FROZEN);
        }
        walletMapper.updateBalance(buyerId, amount.negate(), amount.negate());
        Wallet bAfter = walletMapper.selectById(buyerId);
        recordFlow(buyerId, orderId, FlowType.SETTLEMENT_OUT, amount, bAfter.getBalance());

        // 卖家:balance 加 amount
        Wallet sw = walletMapper.selectByUserIdForUpdate(sellerId);
        if (sw == null) {
            sw = new Wallet();
            sw.setUserId(sellerId);
            sw.setBalance(BigDecimal.ZERO);
            sw.setFrozen(BigDecimal.ZERO);
            walletMapper.insert(sw);
            sw = walletMapper.selectByUserIdForUpdate(sellerId);
        }
        walletMapper.updateBalance(sellerId, amount, BigDecimal.ZERO);
        Wallet sAfter = walletMapper.selectById(sellerId);
        recordFlow(sellerId, orderId, FlowType.SETTLEMENT_IN, amount, sAfter.getBalance());
    }

    public WalletVO me(Long uid) {
        Wallet w = walletMapper.selectById(uid);
        if (w == null) {
            return new WalletVO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        return new WalletVO(w.getBalance(), w.getFrozen(),
                w.getBalance().subtract(w.getFrozen()));
    }

    public PageResult<WalletFlowVO> flows(Long uid, int page, int size) {
        IPage<WalletFlow> p = new Page<>(page, size);
        LambdaQueryWrapper<WalletFlow> q = new LambdaQueryWrapper<WalletFlow>()
                .eq(WalletFlow::getUserId, uid)
                .orderByDesc(WalletFlow::getCreatedAt);
        IPage<WalletFlow> res = flowMapper.selectPage(p, q);
        return PageResult.of(res.getRecords().stream().map(WalletFlowVO::from).toList(),
                res.getTotal(), page, size);
    }

    private void recordFlow(Long uid, Long orderId, FlowType type, BigDecimal amount, BigDecimal balanceAfter) {
        WalletFlow f = new WalletFlow();
        f.setUserId(uid);
        f.setOrderId(orderId);
        f.setType(type);
        f.setAmount(amount);
        f.setBalanceAfter(balanceAfter);
        flowMapper.insert(f);
    }
}