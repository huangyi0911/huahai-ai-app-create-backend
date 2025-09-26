//注意，下面的配置中的一些信息无关紧要，主要是对knife4j主页中的介绍中的一些配置

package com.huahai.huahaiaiappcreate.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * knife4j 配置类
 *
 * @author huahai
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("花海 AI 智能代码应用生成平台") //文档的标题
                        .version("1.0") //文档的版本
                        .description("花海 AI 智能代码应用生成平台接口文档") //文档的介绍
                        .contact(new Contact()
                                .name("huahai")
                                .url("https://github.com/huangyi0911")
                                .email("2909526119@qq.com")
                        ));//文档的作者信息，可以使用默认的无关紧要

    }
}
