package com.huahai.huahaiaiappcreate.core.saver;

import com.huahai.huahaiaiappcreate.ai.model.HtmlCodeResult;
import com.huahai.huahaiaiappcreate.ai.model.MultiFileCodeResult;
import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存器执行器 - 执行器模式
 *
 * @author huahai
 */
public class CodeFileSaverExecutor {

    // 静态 HTML 保存器
    public static final HtmlCodeFileSaver HTML_CODE_FILE_SAVER = new HtmlCodeFileSaver();

    // 静态多文件保存器
    public static final MultiFileCodeSaver MULTI_FILE_CODE_SAVER = new MultiFileCodeSaver();

    /**
     * 执行代码保存器
     *
     * @param result          解析文件结果对象 （HtmlCodeResult 或者 MultiFileCodeResult）
     * @param codeGenTypeEnum 生成类型枚举
     * @return 保存的文件
     */
    public static File executeSave(Object result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 校验参数
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 根据类型生成代码并保存
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_FILE_SAVER.saveCode((HtmlCodeResult) result, appId);
            case MULTI_FILE -> MULTI_FILE_CODE_SAVER.saveCode((MultiFileCodeResult) result, appId);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }
}
