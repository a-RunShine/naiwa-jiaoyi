package com.campus.market.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.* 配置(application.yml 下 app.jwt / app.wechat / app.mock)。
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Wechat wechat, Mock mock) {

    public record Jwt(String secret, long expireHours) {
    }

    public record Wechat(String appId, String appSecret, String cloudEnv, String apiBase) {
    }

    public record Mock(boolean enabled) {
    }
}