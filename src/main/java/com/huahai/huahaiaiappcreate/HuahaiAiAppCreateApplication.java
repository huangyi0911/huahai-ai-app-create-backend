package com.huahai.huahaiaiappcreate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class HuahaiAiAppCreateApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuahaiAiAppCreateApplication.class, args);
    }

}
