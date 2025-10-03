package com.huahai.huahaiaiappcreate.core.handler;

import com.huahai.huahaiaiappcreate.model.entity.User;
import com.huahai.huahaiaiappcreate.model.enums.ChatHistoryMessageTypeEnum;
import com.huahai.huahaiaiappcreate.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单的文本处理器
 * 处理 Html, MultiFile 的生成类型
 *
 * @author huahai
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * Html, MultiFile 的流式处理
     *
     * @param originalFlux       原始流
     * @param chatHistoryService 聊天历史服务接口
     * @param appId              应用 ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originalFlux,
                               ChatHistoryService chatHistoryService,
                               Long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originalFlux.map(content -> {
            // 收集 AI 响应的内容
            aiResponseBuilder.append(content);
            return content;
        }).doOnComplete(() -> {
            // AI 处理完成后，保存 AI 对话信息到数据库
            String aiResponse = aiResponseBuilder.toString();
            chatHistoryService.addChatHistory(aiResponse, appId, loginUser, ChatHistoryMessageTypeEnum.AI.getValue());
        }).doOnError(error -> {
            // 即使 AI 处理出错，也要保存 AI 对话信息到数据库
            String errorMessage = "AI 回复出错，" + error.getMessage();
            chatHistoryService.addChatHistory(errorMessage, appId, loginUser, ChatHistoryMessageTypeEnum.AI.getValue());
        });
    }
}
