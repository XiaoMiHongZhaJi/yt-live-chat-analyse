package com.lwf.ytlivechatanalyse.interceptor;

import com.lwf.ytlivechatanalyse.util.Constant;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;
import java.util.Properties;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DynamicSchemaInterceptor implements Interceptor {

    private static final ThreadLocal<String> schemaContext = new ThreadLocal<>();

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
            connection.setCatalog(schema);
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
