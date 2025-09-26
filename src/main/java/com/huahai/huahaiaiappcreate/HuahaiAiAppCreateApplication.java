package com.huahai.huahaiaiappcreate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启动类
 *
 * @author huahai
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
@MapperScan("com.huahai.huahaiaiappcreate.mapper")
public class HuahaiAiAppCreateApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuahaiAiAppCreateApplication.class, args);
    }

}
