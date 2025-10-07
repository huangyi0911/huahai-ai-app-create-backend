package com.huahai.huahaiaiappcreate.langgraph4j.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * 测试图片收集 AI 服务
 */
@SpringBootTest
class ImageCollectionServiceTest {

    @Resource
    private ImageCollectionService imageCollectionService;

    @Test
    void testTechWebsiteImageCollection() {
        String result = imageCollectionService.collectImages("创建一个技术博客网站，需要展示编程教程和系统架构");
        Assertions.assertNotNull(result);
        System.out.println("技术网站收集到的图片: " + result);
    }

    @Test
    void testEcommerceWebsiteImageCollection() {
        String result = imageCollectionService.collectImages("创建一个电商购物网站，需要展示商品和品牌形象");
        Assertions.assertNotNull(result);
        System.out.println("电商网站收集到的图片: " + result);
    }

    @Test
    void testSimpleWebsiteImageCollection() {
        String result = imageCollectionService.collectImages("创建一个简单的个人博客");
        Assertions.assertNotNull(result);
        System.out.println("个人博客收集到的图片: " + result);
    }
}
