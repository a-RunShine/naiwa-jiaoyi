package com.campus.market.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.file-storage 配置。
 */
@ConfigurationProperties(prefix = "app.file-storage")
public record FileStorageProperties(String dir, String baseUrl) {
}