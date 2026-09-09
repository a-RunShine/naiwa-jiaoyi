package com.campus.market.auth;

import com.campus.market.auth.dto.LoginRequest;
import com.campus.market.auth.dto.LoginResponse;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.GlobalExceptionHandler;
import com.campus.market.config.JwtInterceptor;
import com.campus.market.config.WebConfig;
import com.campus.market.user.dto.UserVO;
import com.campus.market.user.enums.UserRole;
import com.campus.market.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, WebConfig.class, JwtInterceptor.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper om = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    void loginMockUser200() throws Exception {
        UserVO vo = new UserVO(1L, "小白", "http://x/a.png", UserRole.USER, LocalDateTime.now());
        when(authService.login(any(LoginRequest.class))).thenReturn(new LoginResponse("token.x.y", vo));

        LoginRequest req = new LoginRequest("", 1L);
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("token.x.y"))
                .andExpect(jsonPath("$.data.user.nickname").value("小白"));
    }

    @Test
    void loginFailReturnsErrorCode() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED));

        LoginRequest req = new LoginRequest("", 999L);
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }
}