package com.lwf.ytlivechatanalyse.interceptor;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class WebLogAspect {

    private static ThreadLocal<Long> startTime = new ThreadLocal<>();
    @Pointcut("execution(public * com.lwf.ytlivechatanalyse.controller.*.*(..))")
    public void webLog(){
    }

    @Before("webLog()")
    public void doBefore(){
        startTime.set(System.currentTimeMillis());
    }

    public static Long getStartTime(){
        return startTime.get();
    }
}
