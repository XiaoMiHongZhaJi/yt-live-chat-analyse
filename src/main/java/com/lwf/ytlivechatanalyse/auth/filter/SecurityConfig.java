package com.lwf.ytlivechatanalyse.auth.filter;

import com.lwf.ytlivechatanalyse.auth.service.JwtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final String[] whitelist;

    @Autowired
    private AccessDeniedHandler customAccessDeniedHandler;

    // 你的白名单路径（可根据需要修改）
    private static final String[] STATIC_LIST = {
        "/",
        "/favicon.ico",
        "/modules/**",
        "/img/**",
        "/**/*.html",
        "/**/*.js",
        "/**/*.css"
    };

    public SecurityConfig(JwtService jwtService, @Value("${app.security.whitelist:}") String whitelistRaw) {
        this.jwtService = jwtService;
        this.whitelist = whitelistRaw == null ? new String[]{} :
            java.util.Arrays.stream(whitelistRaw.split(",")).map(String::trim).filter(StringUtils::isNotBlank).toArray(String[]::new);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);

        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(STATIC_LIST).permitAll() // ✅ 放行静态资源
            .antMatchers(whitelist).permitAll()   // ✅ 放行静态资源
            .anyRequest().authenticated()         // 其他请求需要认证
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            // 注册自定义异常处理
            .exceptionHandling(exception -> exception.accessDeniedHandler(customAccessDeniedHandler));

        return http.build();
    }
}
