package com.campus.market.wallet;

import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.wallet.entity.Wallet;
import com.campus.market.wallet.entity.WalletFlow;
import com.campus.market.wallet.mapper.WalletFlowMapper;
import com.campus.market.wallet.mapper.WalletMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WalletServiceTest {

    @Mock private WalletMapper walletMapper;
    @Mock private WalletFlowMapper flowMapper;
    @InjectMocks private WalletService walletService;

    @Test
    void freezeInsufficientBalance_throws3001() {
        Wallet w = new Wallet();
        w.setUserId(20L);
        w.setBalance(new BigDecimal("50"));
        w.setFrozen(BigDecimal.ZERO);
        when(walletMapper.selectByUserIdForUpdate(20L)).thenReturn(w);

        assertThatThrownBy(() -> walletService.freeze(20L, 1L, new BigDecimal("100")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.INSUFFICIENT_BALANCE.getCode());
    }

    @Test
    void settle_writesTwoFlows() {
        Wallet bw = new Wallet();
        bw.setUserId(20L);
        bw.setBalance(new BigDecimal("100"));
        bw.setFrozen(new BigDecimal("100"));
        Wallet sw = new Wallet();
        sw.setUserId(10L);
        sw.setBalance(BigDecimal.ZERO);
        sw.setFrozen(BigDecimal.ZERO);
        when(walletMapper.selectByUserIdForUpdate(20L)).thenReturn(bw);
        when(walletMapper.selectByUserIdForUpdate(10L)).thenReturn(sw);
        when(walletMapper.selectById(20L)).thenReturn(bw);
        when(walletMapper.selectById(10L)).thenReturn(sw);

        walletService.settle(20L, 10L, 1L, new BigDecimal("100"));

        ArgumentCaptor<WalletFlow> captor = ArgumentCaptor.forClass(WalletFlow.class);
        verify(flowMapper, times(2)).insert(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getAllValues().get(0).getType().name()).isEqualTo("SETTLEMENT_OUT");
        assertThat(captor.getAllValues().get(1).getType().name()).isEqualTo("SETTLEMENT_IN");
    }
}