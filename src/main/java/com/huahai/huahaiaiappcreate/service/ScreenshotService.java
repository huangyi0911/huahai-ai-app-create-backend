package com.huahai.huahaiaiappcreate.service;

/**
 * 截图服务接口
 *
 * @author huahai
 */
public interface ScreenshotService {

    /**
     * 生成网页截图并上传到 COS 对象存储、
     *
     * @param webUrl 网页地址
     * @return 截图的 COS 访问地址
     */
    String generateAndUploadScreenshot(String webUrl);
}
