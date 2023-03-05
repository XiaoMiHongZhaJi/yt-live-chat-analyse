package com.lwf.ytlivechatanalyse.util;

import java.util.HashMap;
import java.util.Map;

public class Constant {

    /**
     * 分页每页最大大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 批量导入每次导入的条数
     */
    public static final int BATCH_IMPORT_SIZE = 500;

    /**
     * 弹幕分析时每个分段展示的弹幕数
     */
    public static final int ANALYSE_CHAT_COUNT = 40;

    /**
     * 弹幕分析时每个分段单个弹幕最大长度
     */
    public static final int ANALYSE_MESSAGE_LENGTH = 10;

    /**
     * 弹幕分析时合并弹幕的最小长度(只有达到此长度的才会被合并)
     */
    public static final int ANALYSE_MERGE_LENGTH = 2;

    /**
     * 弹幕分析时刷屏弹幕转义
     * 例如 ”？？？？？“、”？？“ 统一合并为 ”？？？“
     */
    public static final Map<String,String> ANALYSE_MERGE_WORD = new HashMap<String,String>(){{
        put("哈","哈哈哈");
        put("美","美美美");
        put("大","大大大");
        put("是","是是是");
        put("啊","啊啊啊啊啊");
        put("？","？？？");
        put("?","？？？");
    }};

    /**
     * 弹幕分析时刷屏弹幕合并比率
     * 例如 ”？“ 在 ”？？？！“ 中占比 75%，判断可以合并为 “？？？”
     */
    public static final int ANALYSE_MERGE_WORD_PROPORTION = 60;

    /**
     * 下载弹幕异常退出时重试次数
     */
    public static final int DOWNLOAD_FAILURE_RETRY_COUNT = 5;
}
