package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class BatchUpdateLiveInfo {

    private static final Logger logger = LoggerFactory.getLogger(BatchUpdateLiveInfo.class);

    public static void main(String[] args) throws Exception{

        List<String> urlList = getUrlList("2023-12-30");
        logger.info("获取到待更新url数量：" + urlList.size());
        CurlUtil.proxy = "http://127.0.0.1:7890";
        for (String url : urlList){
            Map<String, String> liveInfo = CurlUtil.getLiveInfo(url);
            String liveDate = liveInfo.get("liveDate");
            if(StringUtils.isBlank(liveDate)){
                logger.info("无法访问：" + liveDate);
                continue;
            }
            updateLiveInfo(url, liveInfo);
            logger.info("已更新" + liveDate);
            Thread.sleep(1 * 1000 + (int)(Math.random() * 1 * 1000));
        }
    }

    public static void updateLiveInfo(String url, Map<String, String> liveInfo){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String title = liveInfo.get("title");
        String likeCount = liveInfo.get("likeCount");
        String viewCount = liveInfo.get("viewCount");
        String durationTime = liveInfo.get("videoDurationTime");
        String startTimestamp = liveInfo.get("startTimestamp");
        StringBuffer sql = new StringBuffer("update live_info set ");
        List<String> params = new ArrayList<>();
        if(StringUtils.isNotBlank(title)){
            sql.append("title = ?, ");
            params.add(title);
        }
        if(StringUtils.isNotBlank(likeCount)){
            sql.append("like_count = ?, ");
            params.add(likeCount);
        }
        if(StringUtils.isNotBlank(viewCount)){
            sql.append("view_count = ?, ");
            params.add(viewCount);
        }
        if(StringUtils.isNotBlank(durationTime)){
            if(durationTime.split(":").length == 2){
                durationTime = "0:" + durationTime;
            }
            sql.append("duration_time = ?, ");
            params.add(durationTime);
        }
        if(StringUtils.isNotBlank(startTimestamp)){
            sql.append("start_timestamp = ?, ");
            params.add(startTimestamp);
        }
        sql.append("update_time = SYSDATE() ");
        sql.append(" where url = ? ");
        params.add(url);
        JDBCUtil.executeUpdate(sql.toString(), params);
    }

    private static List<String> getUrlList(String start) {
        return getUrlList(start, null);
    }

    public static List<String> getUrlList(String start, String end){
        String sql = "select url from live_info where live_status != '4' and platform = 'y'";
        List<String> params = new ArrayList<>();
        if(StringUtils.isNotBlank(start)){
            sql += "and live_date >= ? ";
            params.add(start);
        }
        if(StringUtils.isNotBlank(end)){
            sql += "and live_date <= ? ";
            params.add(end);
        }
        //当天更新过的不再更新
        sql += "and DATE_FORMAT(SYSDATE(), '%Y-%m-%d') != DATE_FORMAT(update_time, '%Y-%m-%d') ";
        sql += " order by live_date ";
        return JDBCUtil.queryStringList(sql, params);
    }
}
