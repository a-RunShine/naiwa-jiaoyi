package com.campus.market.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 启用 @ConfigurationProperties 绑定 + 显式 ObjectMapper bean。
 */
@Configuration
@EnableConfigurationProperties({AppProperties.class, FileStorageProperties.class})
public class AppConfig {

    /**
     * SB4 启动链路中 JacksonAutoConfiguration 不一定直接注册 ObjectMapper bean,
     * 显式声明以让 MyBatis-Plus / ItemService 等依赖 Jackson 的服务正常装配。
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}