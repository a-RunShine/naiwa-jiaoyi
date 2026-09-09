package com.campus.market.auth;

/**
 * 微信开放接口骨架(MVP 阶段不实现,作为后续接微信云存储的接口锚点)。
 * MVP 默认走 Mock 路径,不在容器中注入 Bean。
 */
public interface WxApiClient {

    /**
     * code2Session:小程序 wx.login() 返回的 code 换 openId。
     */
    String code2Session(String code);

    /**
     * getAccessToken:微信服务端凭证。
     */
    String getAccessToken();

    /**
     * 微信云存储上传。
     */
    String uploadToCloud(byte[] bytes, String filename);
}