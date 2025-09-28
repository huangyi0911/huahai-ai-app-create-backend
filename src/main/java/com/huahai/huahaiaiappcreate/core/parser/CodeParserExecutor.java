package com.huahai.huahaiaiappcreate.core.parser;

import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;

/**
 * 代码解析器执行器 - 执行器模式
 *
 * @author huahai
 */
public class CodeParserExecutor {

    // 静态 HTML 解析器
    public static final HtmlCodeParser HTML_CODE_PARSER = new HtmlCodeParser();

    // 静态多文件解析器
    public static final MultiFileCodeParser MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    /**
     * 执行代码解析器
     *
     * @param content         代码内容
     * @param codeGenTypeEnum 生成类型枚举
     * @return 解析结果 （htmlCodeResult 或 multiFileCodeResult）
     */
    public static Object executeParse(String content, CodeGenTypeEnum codeGenTypeEnum) {
        // 校验参数
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 根据类型生成代码并保存
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_PARSER.parseCode(content);
            case MULTI_FILE -> MULTI_FILE_CODE_PARSER.parseCode(content);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }
}
