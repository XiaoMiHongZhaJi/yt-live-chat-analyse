package com.lwf.ytlivechatanalyse.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class WebLogAspect {
    private final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);

    ThreadLocal<Long> startTime = new ThreadLocal<>();
    @Pointcut("execution(public * com.lwf.ytlivechatanalyse.controller.*.*(..))")
    public void webLog(){
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint){
        startTime.set(System.currentTimeMillis());
        // 接收到请求，记录请求内容
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ipAddress = HttpContextUtils.getIpAddress();
        logger.info("URL : " + request.getRequestURL().toString());
        if(ipAddress.equals("0:0:0:0:0:0:0:1") || ipAddress.equals("127.0.0.1") || ipAddress.equals("localhost")){
            return;
        }
        // 记录下请求内容
        logger.info("HTTP_METHOD : " + request.getMethod());
        logger.info("IP : " + ipAddress);
        logger.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "webLog()")
    public void doAfterReturning(){
        // 处理完请求，返回内容
        logger.info("SPEND TIME : " + (System.currentTimeMillis() - startTime.get()) + "ms");
        logger.info("END");
    }
}
