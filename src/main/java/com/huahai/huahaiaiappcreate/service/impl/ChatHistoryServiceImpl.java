package com.huahai.huahaiaiappcreate.service.impl;

import cn.hutool.core.util.StrUtil;
import com.huahai.huahaiaiappcreate.constants.UserConstant;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.dto.chathistory.ChatHistoryQueryRequest;
import com.huahai.huahaiaiappcreate.model.entity.App;
import com.huahai.huahaiaiappcreate.model.entity.User;
import com.huahai.huahaiaiappcreate.model.enums.ChatHistoryMessageTypeEnum;
import com.huahai.huahaiaiappcreate.service.AppService;
import com.huahai.huahaiaiappcreate.untils.ThrowUtils;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.huahai.huahaiaiappcreate.model.entity.ChatHistory;
import com.huahai.huahaiaiappcreate.mapper.ChatHistoryMapper;
import com.huahai.huahaiaiappcreate.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/huangyi0911">花海</a>
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Lazy
    @Resource
    private AppService appService;

    @Override
    public Boolean addChatHistory(String message, Long appId, User loginUser, String messageType) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(message == null, ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(messageType == null, ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        Long userId = loginUser.getId();
        ThrowUtils.throwIf(userId == null || userId < 0, ErrorCode.PARAMS_ERROR, "用户 ID 错误");
        // 判断消息类型是否存在
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型");
        // 2. 添加对话历史到数据库
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public Boolean deleteChatHistory(Long appId) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        // 2. 删除对话历史
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("appId", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 2. 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 3. 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 4. 查询数据并返回
        // 注意这里默认使用 createTime 作为游标，所以这里需要设置 pageNum 始终等于 1
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public Long loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 1. 校验参数
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
            // 2. 构造查询条件，查询历史记忆
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    // 因为 ai 在于用户对话时默认往记忆里插入了一条数据，所以这里 limit 设置为 1 防止第一条重复利用
                    .limit(1, maxCount);
            List<ChatHistory> chatHistoryList = this.list(queryWrapper);
            if (chatHistoryList.isEmpty()) {
                return 0L;
            }
            // 3. 反转查询结果，保证返回给 ai 的顺序和用户对话顺序一致
            chatHistoryList = chatHistoryList.reversed();
            // 4. 每次加载时，清楚记忆里的缓存
            chatMemory.clear();
            // 5. 遍历查询到结果，根据 messageType 添加到记忆中
            Long loadCount = 0L;
            for (ChatHistory chatHistory : chatHistoryList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(chatHistory.getMessageType())) {
                    chatMemory.add(UserMessage.from(chatHistory.getMessage()));
                }
                if (ChatHistoryMessageTypeEnum.AI.getValue().equals(chatHistory.getMessageType())) {
                    chatMemory.add(AiMessage.from(chatHistory.getMessage()));
                }
                loadCount++;
            }
            // 6. 返回加载的条数
            log.info("成功为 appId 为：{} 的应用加载了 {} 条对话", appId, loadCount);
            return loadCount;
        } catch (Exception e) {
            log.error("加载对话历史失败， 原因为：{}， appId 为： {}", e.getMessage(), appId);
            // 加载失败不影响系统运行，所以这里返回 0
            return 0L;
        }
    }


    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

}
