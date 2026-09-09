package com.campus.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 奶娃二手交易 — Spring Boot 启动类。
 *
 * 启动方式：
 *   1. 本地： ./gradlew bootRun
 *   2. Docker： docker compose up -d --build backend
 *
 * @MapperScan 放在 config.MapperConfig,便于 WebMvcTest 切片时不会加载所有 mapper。
 */
@SpringBootApplication
public class MarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketApplication.class, args);
    }
}