package com.huahai.huahaiaiappcreate.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.huahai.huahaiaiappcreate.ai.AiCodeGeneratorService;
import com.huahai.huahaiaiappcreate.ai.AiCodeGeneratorServiceFactory;
import com.huahai.huahaiaiappcreate.ai.model.HtmlCodeResult;
import com.huahai.huahaiaiappcreate.ai.model.MultiFileCodeResult;
import com.huahai.huahaiaiappcreate.ai.model.message.AiResponseMessage;
import com.huahai.huahaiaiappcreate.ai.model.message.ToolExecutedMessage;
import com.huahai.huahaiaiappcreate.ai.model.message.ToolRequestMessage;
import com.huahai.huahaiaiappcreate.constants.AppConstant;
import com.huahai.huahaiaiappcreate.core.builder.VueProjectBuilder;
import com.huahai.huahaiaiappcreate.core.parser.CodeParserExecutor;
import com.huahai.huahaiaiappcreate.core.saver.CodeFileSaverExecutor;
import com.huahai.huahaiaiappcreate.exception.BusinessException;
import com.huahai.huahaiaiappcreate.exception.ErrorCode;
import com.huahai.huahaiaiappcreate.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
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
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

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
        // 从 AiService 工厂中获取独立的 AiService 实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.aiCodeGeneratorService(appId);
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
        // 从 AiService 工厂中获取独立的 AiService 实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.aiCodeGeneratorService(appId, codeGenTypeEnum);
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
            // 提供 Vue 项目工程模式 (只对流式处理提供 Vue 工程化处理)
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 流式处理 TokenStream 转化为 FluxSteam，传递工具调用消息
     * 适配器模式
     *
     * @param tokenStream tokenStream 对象
     * @return 处理后的 Flux 流对象
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        return Flux.create(sink -> {
            // 监听 TokenStream 流的变化，对流进行处理
            tokenStream.onPartialResponse((String partialResponse) -> {
                // 监听 partialResponse Ai响应消息对象
                AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                sink.next(JSONUtil.toJsonStr(aiResponseMessage));
            }).onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                // 监听 partialToolExecutionRequest 工具调用对象
                ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                sink.next(JSONUtil.toJsonStr(toolRequestMessage));
            }).onToolExecuted((ToolExecution toolExecution) -> {
                // 监听 toolExecuted 工具执行结果对象
                ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
            }).onCompleteResponse((chatResponse) -> {
                // 完成时进行打包项目，实现页面同步展示
                // 在前端处理完成后执行构建 Vue 项目工程
                String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + FileUtil.FILE_SEPARATOR + "vue_project_" + appId;
                // 改为同步打包
                vueProjectBuilder.buildProject(projectPath);
                // 告诉订阅者流处理完成
                sink.complete();
            }).onError((Throwable throwable) -> {
                throwable.printStackTrace();
                sink.error(throwable);
            }).start();
        });
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
