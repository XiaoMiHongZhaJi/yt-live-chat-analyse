package com.lwf.ytlivechatanalyse.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {

    @Bean
    public DynamicSchemaInterceptor dynamicSchemaInterceptor() {
        return new DynamicSchemaInterceptor();
    }
}
