package com.lwf.ytlivechatanalyse.interceptor;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SchemaAspect {

    // 方法执行结束后清理 schema
    @After("execution(* com.lwf.ytlivechatanalyse.service..*(..))")
    public void clearSchema() {
        DynamicSchemaInterceptor.clearSchema();
    }
}

