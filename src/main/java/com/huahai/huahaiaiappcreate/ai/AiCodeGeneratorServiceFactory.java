package com.huahai.huahaiaiappcreate.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.huahai.huahaiaiappcreate.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * AI 代码生成服务工厂
 *
 * @author huahai
 */
@Component
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    // redis 存储对话记忆
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据 appId 创建独立的 AiService
     *
     * @return AiService
     */
    private AiCodeGeneratorService getAiCodeGeneratorService(Long appId) {
        // 根据 appId 获取对应的 AiService
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 在创建 AiService 时，从数据库中导入对应的历史记忆到 chatMemory 中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        // 返回创建的 AiService
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * 根据 appId 获取 AiService
     *
     * @return AiService
     */
    public AiCodeGeneratorService aiCodeGeneratorService(Long appId) {
        // 根据 appId 从本地缓存中取出对应的 AiService 实例，如果不存在则创建并缓存
        return serviceCache.get(appId, this::getAiCodeGeneratorService);
    }

    //    /**
//     * 初始化 AI 代码生成服务
//     *
//     * @return AI 代码生成服务
//     */
//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(streamingChatModel)
//                .build();
//    }

}
