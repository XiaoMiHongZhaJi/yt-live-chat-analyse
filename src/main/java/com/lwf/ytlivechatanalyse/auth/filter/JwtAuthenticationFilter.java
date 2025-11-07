package com.lwf.ytlivechatanalyse.auth.filter;

import com.lwf.ytlivechatanalyse.auth.bean.AuthPrincipal;
import com.lwf.ytlivechatanalyse.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parseToken(token).getBody();
                String userId = claims.get("userId", String.class);
                String userName = claims.get("userName", String.class);
                if (userName != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(new AuthPrincipal(userId, userName), null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                logger.error("JWT 验证失败: " + e.getMessage());
                // 返回 401 状态码（未认证）
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Token无效或已过期\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
