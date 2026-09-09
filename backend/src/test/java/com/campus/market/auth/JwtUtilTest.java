package com.campus.market.auth;

import com.campus.market.config.AppProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil newUtil(long expireHours) {
        AppProperties props = new AppProperties(
                new AppProperties.Jwt("dev-secret-key-must-be-at-least-32-bytes-long-xxxx", expireHours),
                new AppProperties.Wechat("", "", "", ""),
                new AppProperties.Mock(true)
        );
        return new JwtUtil(props);
    }

    @Test
    void signAndParseRoundtrip() {
        JwtUtil jwt = newUtil(24);
        String token = jwt.sign(42L);
        assertThat(token).isNotBlank();
        assertThat(jwt.parse(token)).isEqualTo(42L);
    }

    @Test
    void shortSecretRejected() {
        AppProperties props = new AppProperties(
                new AppProperties.Jwt("short", 24),
                new AppProperties.Wechat("", "", "", ""),
                new AppProperties.Mock(true)
        );
        assertThatThrownBy(() -> new JwtUtil(props))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(">= 32");
    }

    @Test
    void invalidTokenRejected() {
        JwtUtil jwt = newUtil(24);
        assertThatThrownBy(() -> jwt.parse("not-a-jwt"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}