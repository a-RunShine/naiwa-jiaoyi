package com.campus.market.auth;

import com.campus.market.auth.dto.LoginRequest;
import com.campus.market.auth.dto.LoginResponse;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.config.AppProperties;
import com.campus.market.user.dto.UserVO;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final AppProperties props;

    public AuthService(UserMapper userMapper, JwtUtil jwtUtil, AppProperties props) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.props = props;
    }

    /**
     * 登录入口。MVP 阶段:
     *   - mock=true 且 mockUserId != null:用 mockUserId 查 user 表,签 JWT 返
     *   - 否则:留给真实 wx.code2Session(MVP 不实现)
     */
    @Transactional
    public LoginResponse login(LoginRequest req) {
        if (props.mock().enabled() && req.mockUserId() != null) {
            User u = userMapper.selectById(req.mockUserId());
            if (u == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            return new LoginResponse(jwtUtil.sign(u.getId()), UserVO.from(u));
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}