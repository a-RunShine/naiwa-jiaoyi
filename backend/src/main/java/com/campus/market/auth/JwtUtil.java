package com.campus.market.auth;

import com.campus.market.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JWT 签发与解析(JJWT 0.12.7 API)。
 * secret 长度必须 ≥32 字节(HS256 要求),由 application.yml 的 app.jwt.secret 提供。
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMs;

    public JwtUtil(AppProperties props) {
        byte[] bytes = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret 必须 >= 32 字节(HS256 要求)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expireMs = Duration.ofHours(props.jwt().expireHours()).toMillis();
    }

    /**
     * 签发 token,subject = userId(字符串)。
     */
    public String sign(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析 token 拿 userId。失败抛 JwtException(由 JwtInterceptor 捕获转 1001)。
     */
    public Long parse(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        return Long.valueOf(jws.getPayload().getSubject());
    }
}