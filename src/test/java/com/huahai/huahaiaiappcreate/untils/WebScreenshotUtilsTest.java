package com.huahai.huahaiaiappcreate.untils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class WebScreenshotUtilsTest {


    /**
     * 测试网页截图功能
     */
    @Test
    void saveWebPageScreenshot() {
        String testUrl = "https://github.com/huangyi0911";
        String webPageScreenshot = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
        Assertions.assertNotNull(webPageScreenshot);
    }


}