package com.huahai.huahaiaiappcreate.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * HTML 代码结果封装
 *
 * @author huahai
 */
@Description("HTML 代码生成结果")
@Data
public class HtmlCodeResult {

    @Description("生成的 HTML 代码")
    private String htmlCode;

    @Description("生成的代码的描述")
    private String description;
}
