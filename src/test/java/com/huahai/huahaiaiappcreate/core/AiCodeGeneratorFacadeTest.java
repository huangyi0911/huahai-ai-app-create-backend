package com.huahai.huahaiaiappcreate.core;

import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

/**
 * 测试 AI 代码生成器外观类
 */
@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    /**
     * 测试生成并保存代码
     */
    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("帮我生成一个用户名为花海的个人留言板网站，不超过 50 行代码", CodeGenTypeEnum.HTML);
        Assertions.assertNotNull(file);
    }

    /**
     * 测试生成并保存代码（流式）
     */
    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("帮我生成一个用户名为骚包的个人博客，要求页面要美观简约，且有图片的轮播，背景可以采用渐变色", CodeGenTypeEnum.MULTI_FILE);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

}
