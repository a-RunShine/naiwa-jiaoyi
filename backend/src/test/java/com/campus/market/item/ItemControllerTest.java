package com.campus.market.item;

import com.campus.market.common.GlobalExceptionHandler;
import com.campus.market.common.PageResult;
import com.campus.market.config.JwtInterceptor;
import com.campus.market.config.WebConfig;
import com.campus.market.item.dto.ItemQueryRequest;
import com.campus.market.item.entity.Item;
import com.campus.market.item.enums.ItemStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import({GlobalExceptionHandler.class, WebConfig.class, JwtInterceptor.class})
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private com.campus.market.auth.JwtUtil jwtUtil;

    @Test
    void listPublic_noAuth_returns200() throws Exception {
        Item sample = new Item();
        sample.setId(1L);
        sample.setTitle("hello");
        sample.setStatus(ItemStatus.ON_SALE);
        sample.setPrice(new BigDecimal("10.00"));
        when(itemService.list(any(ItemQueryRequest.class)))
                .thenReturn(PageResult.of(List.of(sample), 1, 1, 20));

        mvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void create_requires_auth_code() throws Exception {
        mvc.perform(post("/api/items")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }
}