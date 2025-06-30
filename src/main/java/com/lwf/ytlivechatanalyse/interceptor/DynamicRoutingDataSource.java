package com.lwf.ytlivechatanalyse.interceptor;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicSchemaInterceptor.getSchema();
    }
}
