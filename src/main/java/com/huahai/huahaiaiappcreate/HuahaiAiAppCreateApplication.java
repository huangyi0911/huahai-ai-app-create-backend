package com.huahai.huahaiaiappcreate;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启动类
 *
 * @author huahai
 */
@EnableCaching
@EnableAspectJAutoProxy(exposeProxy = true)
// 排除 RedisEmbeddingStoreAutoConfiguration，因为 RedisChatMemoryStoreConfig 已经配置了 RedisEmbeddingStoreAutoConfiguration
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.huahai.huahaiaiappcreate.mapper")
public class HuahaiAiAppCreateApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuahaiAiAppCreateApplication.class, args);
    }

}
