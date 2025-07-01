package com.lwf.ytlivechatanalyse.interceptor;

import com.lwf.ytlivechatanalyse.util.Constant;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        String dbKey = DynamicSchemaInterceptor.getSchema();
        return dbKey != null ? dbKey : Constant.DEFAULT_SCHEMA;
    }
}
