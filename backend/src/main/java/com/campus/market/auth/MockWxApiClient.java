package com.campus.market.auth;

import org.springframework.stereotype.Component;

/**
 * MVP 占位实现:不支持真实微信流程。
 * 注意:**不注册为 Bean**(@Component 保留仅为 IDE 索引方便,实际 AuthService 不引用)。
 * 真实接入时新增 {@code RealWxApiClient} 并通过 @ConditionalOnProperty 控制启用。
 */
@Component
public class MockWxApiClient implements WxApiClient {

    @Override
    public String code2Session(String code) {
        throw new UnsupportedOperationException("MVP 阶段未对接真实微信,AppID/Secret 占位请走 mock-login");
    }

    @Override
    public String getAccessToken() {
        throw new UnsupportedOperationException("MVP 阶段未对接真实微信");
    }

    @Override
    public String uploadToCloud(byte[] bytes, String filename) {
        throw new UnsupportedOperationException("MVP 阶段未对接微信云存储");
    }
}