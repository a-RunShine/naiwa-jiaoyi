package com.campus.market.wallet;

import com.campus.market.common.GlobalExceptionHandler;
import com.campus.market.common.PageResult;
import com.campus.market.config.JwtInterceptor;
import com.campus.market.config.WebConfig;
import com.campus.market.wallet.entity.WalletFlow;
import com.campus.market.wallet.enums.FlowType;
import com.campus.market.wallet.vo.WalletFlowVO;
import com.campus.market.wallet.vo.WalletVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@Import({GlobalExceptionHandler.class, WebConfig.class, JwtInterceptor.class})
class WalletControllerTest {

    @Autowired
    private MockMvc mvc;
    private final ObjectMapper om = new ObjectMapper();

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private com.campus.market.auth.JwtUtil jwtUtil;

    @Test
    void me_requires_auth_code() throws Exception {
        mvc.perform(get("/api/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void recharge_requires_auth_code() throws Exception {
        mvc.perform(post("/api/wallet/recharge")
                        .contentType("application/json")
                        .content("{\"amount\": 100.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }
}