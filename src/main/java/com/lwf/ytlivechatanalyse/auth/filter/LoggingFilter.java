package com.lwf.ytlivechatanalyse.auth.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lwf.ytlivechatanalyse.auth.bean.AuthPrincipal;
import com.lwf.ytlivechatanalyse.auth.bean.LogEntity;
import com.lwf.ytlivechatanalyse.auth.dao.LogMapper;
import com.lwf.ytlivechatanalyse.interceptor.WebLogAspect;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Autowired
    private LogMapper logRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public LoggingFilter(LogMapper logRepo) {
        this.logRepo = logRepo;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        BufferedResponseWrapper wrappedResp = new BufferedResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, wrappedResp);
        byte[] bytes = wrappedResp.getBuffer();
        String respBody = bytes.length > 0 ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8) : "";
        String requestURI = req.getRequestURI();

        Integer returnCount = null;
        try {
            JsonNode parsed = mapper.readTree(respBody);
            if (parsed.isArray()) {
                returnCount = parsed.size();
            } else if (parsed.isObject()) {
                returnCount = 1;
                if (parsed.has("data") && parsed.get("data").isArray()) {
                    returnCount = parsed.get("data").size();
                }
            }
        } catch (Exception ignored) {
            logger.warn("not json: {}", respBody);
        }
        String params;
        if (StringUtils.isBlank(req.getQueryString())) {
            params = mapper.writeValueAsString(req.getParameterMap());
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("ParameterMap", req.getParameterMap());
            result.put("QueryString", req.getQueryString());
            params = mapper.writeValueAsString(result);
        }

        String truncated = respBody.length() > 200 ? respBody.substring(0, 197) + "..." : respBody;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;
        String userName = null;
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal) {
            AuthPrincipal principal = (AuthPrincipal) auth.getPrincipal();
            userId = principal.getUserId();
            userName = principal.getUserName();
        }

        LogEntity logEntity = new LogEntity();
        logEntity.setUserId(userId);
        logEntity.setUserName(userName);
        logEntity.setIp(getIpAddress(req));
        logEntity.setMethod(req.getMethod());
        logEntity.setUrl(requestURI);
        logEntity.setParams(params);
        logEntity.setReturnCount(returnCount);
        logEntity.setReturnContent(truncated);
        Long startTime = WebLogAspect.getStartTime();
        if (startTime != null) {
            logEntity.setSpendTime(System.currentTimeMillis() - startTime);
        }
        logger.info(logEntity.toString());
        try {
            logRepo.insert(logEntity);
        } catch (Exception e) {
            logger.error("插入日志出错", e);
        }

        ServletOutputStream out = response.getOutputStream();
        out.write(bytes);
        out.flush();
    }
    public static String getIpAddress(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip)){
            ip = request.getHeader("Proxy-Client-IP");
            if (StringUtils.isBlank(ip)){
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isBlank(ip)){
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isBlank(ip)){
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
            }
        }
        if (StringUtils.isNotBlank(ip) && ip.length() > 45){
            ip = ip.substring(0, 45);
        }
        return ip;
    }
}
