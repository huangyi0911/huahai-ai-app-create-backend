package com.huahai.huahaiaiappcreate.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成类型路由服务工厂
 *
 * @author yupi
 */
@Slf4j
@Configuration
public class AiCodeGeneratorTypeRoutingServiceFactory {

    @Resource
    private ChatModel chatModel;

    /**
     * 创建AI代码生成类型路由服务实例
     */
    @Bean
    public AiCodeGeneratorTypeRoutingService aiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGeneratorTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }
}
