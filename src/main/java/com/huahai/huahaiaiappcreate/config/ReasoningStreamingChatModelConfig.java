package com.huahai.huahaiaiappcreate.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 深度推理对话模型初始化配置（流式）
 *
 * @author huahai
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    private String apiKey;

    private String baseUrl;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        // 为了测试方便，开发环境使用普通模型
        final String modelName = "deepseek-chat";
        final int maxToken = 8192;
        // 生成环境使用 deepseek 的推理模型
        // final String modelName = "deepseek-reasoner";
        // final int maxToken = 32768;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxToken)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
