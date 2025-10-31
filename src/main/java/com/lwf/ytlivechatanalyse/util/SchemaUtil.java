package com.lwf.ytlivechatanalyse.util;

import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.bean.SrtData;
import com.lwf.ytlivechatanalyse.interceptor.DynamicSchemaInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SchemaUtil {

    private static final Logger logger = LoggerFactory.getLogger(SchemaUtil.class);
    public static void setSchema(String schema) {
        if (StringUtils.isBlank(schema) || schema.startsWith(Constant.DEFAULT_YEAR)) {
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA);
            logger.warn("setSchema schema: {}", Constant.DEFAULT_SCHEMA);
            return;
        }
        if (schema.length() > 4) {
            schema = schema.substring(0, 4);
        }
        DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + schema);
        logger.warn("setSchema schema: {}", schema);
    }

    public static void setSchema(LiveInfo liveInfo) {
        if (liveInfo != null) {
            String schema = liveInfo.getSchema();
            if (StringUtils.isNotBlank(schema)) {
                setSchema(schema);
                return;
            }
            String liveDate = liveInfo.getLiveDate();
            if (StringUtils.isNotBlank(liveDate)) {
                setSchema(liveDate);
                return;
            }
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA);
            logger.warn("setSchema liveInfo: {}", Constant.DEFAULT_SCHEMA);
            return;
        }
        DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA);
        logger.warn("setSchema liveInfo: {}", Constant.DEFAULT_SCHEMA);
    }

    public static void setSchema(LiveChatData liveChatData) {
        if (liveChatData == null) {
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA);
            logger.warn("setSchema liveChatData: {}", Constant.DEFAULT_SCHEMA);
            return;
        }
        String schema = liveChatData.getSchema();
        if (StringUtils.isNotBlank(schema)) {
            setSchema(schema);
            return;
        }
        String liveDate = liveChatData.getLiveDate();
        if (StringUtils.isNotBlank(liveDate)) {
            setSchema(liveDate);
            return;
        }
        DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA);
        logger.warn("setSchema liveChatData: {}", Constant.DEFAULT_SCHEMA);
    }

    public static void setSchema(SrtData srtData) {
        if (srtData == null) {
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA);
            logger.warn("setSchema srtData: {}", Constant.DEFAULT_SCHEMA);
            return;
        }
        String schema = srtData.getSchema();
        if (StringUtils.isNotBlank(schema)) {
            setSchema(schema);
            return;
        }
        String liveDate = srtData.getLiveDate();
        if (StringUtils.isNotBlank(liveDate)) {
            setSchema(liveDate);
            return;
        }
        DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA);
        logger.warn("setSchema srtData: {}", Constant.DEFAULT_SCHEMA);
    }
}
