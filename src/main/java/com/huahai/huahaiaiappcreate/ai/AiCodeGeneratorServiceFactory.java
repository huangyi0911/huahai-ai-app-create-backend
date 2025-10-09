package com.huahai.huahaiaiappcreate.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.huahai.huahaiaiappcreate.ai.guardrail.PromptSafetyInputGuardrail;
import com.huahai.huahaiaiappcreate.ai.tools.*;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;
import com.huahai.huahaiaiappcreate.service.ChatHistoryService;
import com.huahai.huahaiaiappcreate.untils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
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

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    // redis 存储对话记忆
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
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
    private AiCodeGeneratorService getAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenType) {
        // 根据 appId 获取对应的 AiService
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                // 调整保存的历史对话数量，避免历史对话过少导致 ai 模型陷入死循环
                .maxMessages(50)
                .build();
        // 在创建 AiService 时，从数据库中导入对应的历史记忆到 chatMemory 中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        // 根据代码类型生成不同的模型配置
        // 返回创建的 AiService
        return switch (codeGenType) {
            // vue 项目工程化代码使用推理模型
            case VUE_PROJECT -> {
                // 获取深度推理模型（流式）
                StreamingChatModel reasoningStreamingChatModelPrototype = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .streamingChatModel(reasoningStreamingChatModelPrototype)
                        .chatMemoryProvider(memoryId -> chatMemory)
                        .tools(toolManager.getAllTools())
                        // 添加输入护轨，防止 prompt 中包含敏感词
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        //  hallucinatedToolNameStrategy 配置，当调用的 tool 不存在时，返回错误信息
                        .hallucinatedToolNameStrategy(toolExecutionRequest -> {
                            return ToolExecutionResultMessage.from(
                                    toolExecutionRequest,
                                    "Error: there is no tool called: " + toolExecutionRequest.name()
                            );
                        })
                        .build();
            }
            // Html， 多文件代码使用普通模型
            case HTML, MULTI_FILE -> {
                // 获取流式输出模型
                StreamingChatModel streamingChatModelPrototype = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(streamingChatModelPrototype)
                        .chatMemory(chatMemory)
                        // 添加输入护轨，防止 prompt 中包含敏感词
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        .build();
            }

        };
    }

    /**
     * 根据 appId 获取 AiService
     *
     * @return AiService
     */
    public AiCodeGeneratorService aiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenType) {
        // 构造缓存键
        String cacheKey = this.buildCacheKey(appId, codeGenType);
        // 根据 appId 从本地缓存中取出对应的 AiService 实例，如果不存在则创建并缓存
        return serviceCache.get(cacheKey, key -> getAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 根据 appId 获取 AiService （为了兼容老逻辑，提供一个构造 Ai Service 的方法， 无缓存）
     *
     * @return AiService
     */
    public AiCodeGeneratorService aiCodeGeneratorService(Long appId) {
        // 根据 appId 从本地缓存中取出对应的 AiService 实例，如果不存在则创建并缓存
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 构建缓存键
     *
     * @param appId 应用 ID
     * @return 缓存键
     */
    private String buildCacheKey(Long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
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
