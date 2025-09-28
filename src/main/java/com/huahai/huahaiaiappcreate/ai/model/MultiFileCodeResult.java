package com.huahai.huahaiaiappcreate.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 多文件生成结果封装
 *
 * @author huahai
 */
@Description("多文件代码生成的结果")
@Data
public class MultiFileCodeResult {

    @Description("生成的 HTML 代码")
    private String htmlCode;

    @Description("生成的 CSS 样式代码")
    private String cssCode;

    @Description("生成的 JavaScript 代码")
    private String jsCode;

    @Description("生成的代码的描述")
    private String description;
}
