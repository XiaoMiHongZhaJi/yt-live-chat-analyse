package com.lwf.ytlivechatanalyse.auth.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    private final LoggingFilter loggingFilter;

    public FilterConfig(LoggingFilter loggingFilter) { this.loggingFilter = loggingFilter; }

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilterRegistration() {
        FilterRegistrationBean<LoggingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(loggingFilter);
        reg.addUrlPatterns("/api/*"); // 记录 /api/*
        reg.setOrder(2); // 顺序，确保在 Spring Security 之后（security filter 为 -100）
        return reg;
    }
}
