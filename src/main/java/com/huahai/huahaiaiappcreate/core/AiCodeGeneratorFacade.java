package com.huahai.huahaiaiappcreate.core;

import com.huahai.huahaiaiappcreate.ai.AiCodeGeneratorService;
import com.huahai.huahaiaiappcreate.ai.model.HtmlCodeResult;
import com.huahai.huahaiaiappcreate.ai.model.MultiFileCodeResult;
import com.huahai.huahaiaiappcreate.core.parser.CodeParserExecutor;
import com.huahai.huahaiaiappcreate.core.saver.CodeFileSaverExecutor;
import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 *
 * @author huahai
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 校验参数
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 根据类型生成代码并保存
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSave(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSave(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码 （流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 校验参数
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 根据类型生成代码并保存
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 通用的流式代码处理
     *
     * @param codeStream      代码流
     * @param codeGenTypeEnum 生成类型枚举
     * @return 处理后的流
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 创建代码构建器
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段拼接进 codeBuilder 中
        return codeStream.doOnNext(codeBuilder::append).doOnComplete(() -> {
            try {
                String codeStr = codeBuilder.toString();
                // 解析 HTML 代码
                Object result = CodeParserExecutor.executeParse(codeStr, codeGenTypeEnum);
                // 保存 HTML 到文件中
                File codeFile = CodeFileSaverExecutor.executeSave(result, codeGenTypeEnum, appId);
                log.info("HTML 代码保存成功, 路径为： {}", codeFile.getAbsolutePath());
            } catch (Exception e) {
                log.error("HTML 代码保存失败， 原因为：", e);
            }
        });
    }


    // 已用模板设计模式优化
    /**
     * 生成流式 HTML 代码并保存
     *
     * @param userMessage 用户提示词
     * @return 响应流
     *//*
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        // 生成流式调用代码
        Flux<String> fluxResult = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        // 在调用结束后进行解析
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段拼接进 codeBuilder 中
        return fluxResult.doOnNext(codeBuilder::append).doOnComplete(() -> {
            try {
                String codeStr = codeBuilder.toString();
                // 解析 HTML 代码
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(codeStr);
                // 保存 HTML 到文件中
                File codeFile = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                log.info("HTML 代码保存成功, 路径为： {}", codeFile.getAbsolutePath());
            } catch (Exception e) {
                log.error("HTML 代码保存失败， 原因为：", e);
            }
        });
    }

    *//**
     * 生成流式多文件代码并保存
     *
     * @param userMessage 用户提示词
     * @return 响应流
     *//*
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        // 生成流式调用代码
        Flux<String> fluxResult = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        // 在调用结束后进行解析
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段拼接进 codeBuilder 中
        return fluxResult.doOnNext(codeBuilder::append).doOnComplete(() -> {
            try {
                String codeStr = codeBuilder.toString();
                // 解析 HTML 代码
                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(codeStr);
                // 保存 HTML 到文件中
                File codeFile = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
                log.info("多文件代码保存成功, 路径为： {}", codeFile.getAbsolutePath());
            } catch (Exception e) {
                log.error("多文件代码保存失败， 原因为：", e);
            }
        });
    }

    *//**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     *//*
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    *//**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     *//*
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }*/
}
