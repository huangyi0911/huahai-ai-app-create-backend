package com.huahai.huahaiaiappcreate.ai;

import com.huahai.huahaiaiappcreate.ai.model.HtmlCodeResult;
import com.huahai.huahaiaiappcreate.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 AI 代码生成服务
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 测试生成 HTML 代码
     */
    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("帮我生成一个花海的个人博客网站，不超过 20 行代码");
        Assertions.assertNotNull(result);
    }

    /**
     * 测试生成多文件代码
     */
    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("帮我生成一个花海的留言板，不超过 20 行代码");
        Assertions.assertNotNull(result);
    }
}