package com.huahai.huahaiaiappcreate.langgraph4j.node;

import com.huahai.huahaiaiappcreate.ai.AiCodeGeneratorTypeRoutingServiceFactory;
import com.huahai.huahaiaiappcreate.langgraph4j.state.WorkflowContext;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;
import com.huahai.huahaiaiappcreate.untils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 智能路由节点（根据创建应用的类型选择要路由的模式）
 *
 * @author huahai
 */
@Slf4j
public class RouterNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");

            CodeGenTypeEnum generationType;
            try {
                // 获取 AI 路由服务
                AiCodeGeneratorTypeRoutingServiceFactory factory = SpringContextUtil.getBean(AiCodeGeneratorTypeRoutingServiceFactory.class);
                // 根据原始提示词进行智能路由
                generationType = factory.createAiCodeGenTypeRoutingService().getCodeGenTypeRouting(context.getOriginalPrompt());
                log.info("AI智能路由完成，选择类型: {} ({})", generationType.getValue(), generationType.getText());
            } catch (Exception e) {
                log.error("AI智能路由失败，使用默认HTML类型: {}", e.getMessage());
                generationType = CodeGenTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            return WorkflowContext.saveContext(context);
        });
    }
}

