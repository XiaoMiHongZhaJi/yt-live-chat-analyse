package com.lwf.ytlivechatanalyse.interceptor;

import com.lwf.ytlivechatanalyse.util.Constant;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Properties;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DynamicSchemaInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(DynamicSchemaInterceptor.class);

    private static final ThreadLocal<String> schemaContext = new ThreadLocal<>();

    private int clientId = 0;

    public static void setSchema(String schema) {
        schemaContext.set(schema);
    }

    public static String getSchema() {
        return schemaContext.get() == null ? Constant.DEFAULT_SCHEMA : schemaContext.get();
    }

    public static void clearSchema() {
        schemaContext.remove();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String schema = getSchema();
        if (schema != null) {
            Connection connection = (Connection) invocation.getArgs()[0];
            if(connection.getClientInfo("connectionId") == null) {
                String connectionId = clientId++ + "";
                connection.setClientInfo("connectionId", connectionId);
                logger.info("set connectionId: {}", connectionId);
            }
            connection.setCatalog(schema);
            logger.info("connectionId: {}, database: {}", connection.getClientInfo("connectionId"), schema);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
