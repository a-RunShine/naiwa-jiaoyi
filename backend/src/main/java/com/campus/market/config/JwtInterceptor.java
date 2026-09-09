package com.campus.market.config;

import com.campus.market.auth.JwtUtil;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.UserContext;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Pattern;

/**
 * 鉴权拦截器。所有 /api/** 请求除白名单外必须带 Authorization: Bearer &lt;jwt&gt;。
 *
 * 白名单(全部 GET 限定):
 *   /api/auth/**
 *   GET /api/items
 *   GET /api/items/{id}
 *   /swagger-ui/**
 *   /v3/api-docs/**
 *   /actuator/**
 *   /uploads/**
 *
 * GET 白名单通过本类方法内 inline 判定(method + path);其他路径白名单在 WebConfig 的
 * excludePathPatterns 里排除;剩下全部强制鉴权。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Pattern ITEM_DETAIL_PATTERN = Pattern.compile("^/api/items/\\d+$");

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // GET 商品列表与详情放行(游客白名单)
        if ("GET".equalsIgnoreCase(method)
                && (path.equals("/api/items") || ITEM_DETAIL_PATTERN.matcher(path).matches())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String token = header.substring("Bearer ".length()).trim();
        try {
            Long userId = jwtUtil.parse(token);
            UserContext.set(userId);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}