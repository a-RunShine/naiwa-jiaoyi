package com.campus.market.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus mapper 扫描配置。
 * 独立成类是为了避免 {@code @WebMvcTest} 切片时连带加载所有 mapper。
 */
@Configuration
@MapperScan("com.campus.market.**.mapper")
public class MapperConfig {
}