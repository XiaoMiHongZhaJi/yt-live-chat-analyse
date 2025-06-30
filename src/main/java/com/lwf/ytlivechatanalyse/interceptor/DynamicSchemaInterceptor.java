package com.lwf.ytlivechatanalyse.interceptor;


public class DynamicSchemaInterceptor {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setSchema(String db) {
        CONTEXT_HOLDER.set(db);
    }

    public static String getSchema() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearSchema() {
        CONTEXT_HOLDER.remove();
    }
}
