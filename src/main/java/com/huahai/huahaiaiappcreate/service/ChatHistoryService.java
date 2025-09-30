package com.huahai.huahaiaiappcreate.service;

import com.huahai.huahaiaiappcreate.model.dto.chathistory.ChatHistoryQueryRequest;
import com.huahai.huahaiaiappcreate.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.huahai.huahaiaiappcreate.model.entity.ChatHistory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/huangyi0911">花海</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {


    /**
     * 添加对话历史
     *
     * @param message     消息内容
     * @param appId       应用 ID
     * @param loginUser   登录用户
     * @param messageType 消息类型
     * @return 是否添加成功
     */
    Boolean addChatHistory(String message, Long appId, User loginUser, String messageType);

    /**
     * 删除对话历史（根据关联的应用 ID）
     *
     * @param appId 应用 ID
     * @return 是否删除成功
     */
    Boolean deleteChatHistory(Long appId);

    /**
     * 分页查询对话历史
     *
     * @param appId          应用 ID
     * @param pageSize       每页查询到数量
     * @param lastCreateTime 最后创建时间 - 游标查询，只根据该时间进行查询，不根据页码数查询
     * @param loginUser      登录用户
     * @return 分页查询结果
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 加载对话历史到 redis 对话记忆存储器中
     *
     * @param appId 应用 ID
     * @param chatMemory 对话记忆存储器
     * @return 加载历史对话的数量
     */
    Long loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 构造对话历史查询条件
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return 查询条件封装
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
