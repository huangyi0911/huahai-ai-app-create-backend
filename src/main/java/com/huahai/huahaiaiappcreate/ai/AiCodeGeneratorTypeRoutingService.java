package com.huahai.huahaiaiappcreate.ai;

import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI 代码生成类型路由服务接口
 *
 * @author huahai
 */
public interface AiCodeGeneratorTypeRoutingService {

    /**
     * 让 AI 对话来判断生成代码的类型
     *
     * @param userMessage 用户输入的提示词
     * @return 生成的代码类型
     */
    @SystemMessage(fromResource = "prompt/codegen-type-routing-system-prompt.txt")
    CodeGenTypeEnum getCodeGenTypeRouting(String userMessage);
}
