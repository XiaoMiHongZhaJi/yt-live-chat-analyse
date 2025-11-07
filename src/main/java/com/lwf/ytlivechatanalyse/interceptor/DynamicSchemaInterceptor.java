package com.lwf.ytlivechatanalyse.interceptor;

import com.lwf.ytlivechatanalyse.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DynamicSchemaInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HandlerInterceptor.class);

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    private static final String INCLUDE_PATHS = "/api";

    private static final String EXCLUDE_PATHS = "/api/auth";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith(EXCLUDE_PATHS) || !requestURI.startsWith(INCLUDE_PATHS)) {
            return true;
        }

        String schema = request.getHeader("X-Schema");
        if (StringUtils.isBlank(schema)) {
            schema = request.getParameter("schema");
        }

        if (StringUtils.isNotBlank(schema)) {
            if (schema.startsWith(Constant.DEFAULT_YEAR)) {
                setSchema(Constant.DEFAULT_SCHEMA);
                logger.warn("setSchema schema: {}", Constant.DEFAULT_SCHEMA);
                return true;
            }
            if (schema.length() > 4) {
                schema = schema.substring(0, 4);
            }
            setSchema(Constant.DEFAULT_SCHEMA + "_" + schema);
            logger.warn("setSchema schema: {}", schema);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CONTEXT_HOLDER.remove();
    }

    public static void setSchema(String db) {
        CONTEXT_HOLDER.set(db);
    }

    public static String getSchema() {
        return CONTEXT_HOLDER.get();
    }
}
