package com.huahai.huahaiaiappcreate.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具管理器
 * 管理 AI 工具
 *
 * @author huahai
 */
@Slf4j
@Component
public class ToolManager {

    /**
     * 定义存储工具集合，通过工具名称进行映射
     */
    public final Map<String, BaseTool> toolMap = new HashMap<>();

    /**
     * 自动注入所有 AI 工具
     */
    @Resource
    private BaseTool[] baseTools;

    /**
     * 初始化所有工具
     * @PostConstruct 使该方法在 Bean 初始化之后立即执行
     */
    @PostConstruct
    public void initTools() {
        for (BaseTool baseTool : baseTools) {
            toolMap.put(baseTool.getToolName(), baseTool);
            log.info("注册初始化工具: {} -> {}", baseTool.getToolName(), baseTool.getDisplayName());
        }
        log.info("工具初始化完成，数量为：{}", toolMap.size());
    }

    /**
     * 通过工具名称获取工具实例
     *
     * @param toolName 工具名称
     * @return 工具实例
     */
    public BaseTool getToolByToolName(String toolName) {
        return toolMap.get(toolName);
    }

    /**
     * 获取已经注册的所有工具列表
     * 返回的必须是数组，如果单个实例对象 langchain4j 创建的 AI Service 会无法正确注入
     *
     * @return 工具列表
     */
    public BaseTool[] getAllTools(){
        return baseTools;
    }
}
