package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

/* //获取video url列表，需要先引入jQuery
var arr = [];
$("#contents").each(function(){
    $(this).find("ytd-rich-grid-media").each(function(){
        var a = $(this).find("#video-title-link");
        arr.push("https://www.youtube.com" + a.attr("href").split("&")[0]);
    })
})
console.log(arr.join("\n"));
*/

public class BatchUpdateVideoInfo {

    private static final Logger logger = LoggerFactory.getLogger(BatchUpdateVideoInfo.class);

    public static void main(String[] args) throws Exception{

        List<String> urlList = getUrlList();
        logger.info("获取到待更新url数量：" + urlList.size());
        for (String url : urlList){
            Map<String, String> liveInfo = CurlUtil.getLiveInfo(url);
            updateLiveInfo(url, liveInfo);
            logger.info("已更新" + liveInfo.get("liveDate"));
            Thread.sleep(3 * 1000 + (int)(Math.random() * 5 * 1000));
        }
    }

    public static void updateLiveInfo(String url, Map<String, String> liveInfo){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String title = liveInfo.get("title");
        String likeCount = liveInfo.get("likeCount");
        String viewCount = liveInfo.get("viewCount");
        String startTimestamp = liveInfo.get("startTimestamp");
        String endTimestamp = liveInfo.get("endTimestamp");
        String durationTime = liveInfo.get("videoDurationTime");
        String publishDate = liveInfo.get("publishDate");
        String commentCount = liveInfo.get("commentCount");
        StringBuffer sql = new StringBuffer("update video_info set ");
        List<String> params = new ArrayList<>();
        if(StringUtils.isNotBlank(publishDate)){
            sql.append("live_date = ?, ");
            params.add(publishDate);
        }
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
            sql.append("duration_time = ?, ");
            params.add(durationTime);
        }
        if(StringUtils.isNotBlank(commentCount)){
            sql.append("comment_count = ?, ");
            params.add(commentCount);
        }
        sql.append("update_time = SYSDATE() ");
        sql.append(" where url = ? ");
        params.add(url);
        executeUpdate(sql.toString(), params);
    }

    private static List<String> getUrlList() {
        return getUrlList(null, null);
    }

    public static List<String> getUrlList(String start, String end){
        String sql = "select url from video_info where 1 = 1 ";
        List<String> params = new ArrayList<>();
        if(StringUtils.isNotBlank(start)){
            sql += "and live_date >= ? ";
            params.add(start);
        }
        if(StringUtils.isNotBlank(end)){
            sql += "and live_date <= ? ";
            params.add(end);
        }
//        sql += "and (like_count like '%K%' or comment_count like '%K%') ";
        sql += "and (update_time is null or DATE_FORMAT(SYSDATE(), '%Y-%m-%d') != DATE_FORMAT(update_time, '%Y-%m-%d')) ";
        sql += "order by live_date ";
        return executeQueryList(sql, params);
    }

    public static List<String> executeQueryList(String sql, List<String> params){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<String> list = new ArrayList<>();
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);
            if(!CollectionUtils.isEmpty(params)){
                for (int i = 0; i < params.size(); i ++) {
                    String param = params.get(i);
                    preparedStatement.setString(i + 1, param);
                }
            }
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                list.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            logger.error("执行sql失败");
            e.printStackTrace();
        } finally {
            try {
                if(resultSet != null){
                    resultSet.close();
                }
                if(preparedStatement != null){
                    preparedStatement.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (Exception e) {
                logger.error("关闭失败");
            }
        }
        return list;
    }

    public static void executeUpdate(String sql, List<String> params){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);
            if(!CollectionUtils.isEmpty(params)){
                for (int i = 0; i < params.size(); i ++) {
                    String param = params.get(i);
                    preparedStatement.setString(i + 1, param);
                }
            }
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            logger.error("执行sql失败");
            e.printStackTrace();
        } finally {
            try {
                if(preparedStatement != null){
                    preparedStatement.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (Exception e) {
                logger.error("关闭connection失败");
            }
        }
    }

    public static Connection getConnection(){
        Properties properties = new Properties();
        Connection connection = null;
        try {
            properties.load(new BufferedInputStream(new FileInputStream("src/main/resources/application.properties")));
            String url = properties.getProperty("spring.datasource.url");
            String username = properties.getProperty("spring.datasource.username");
            String password = properties.getProperty("spring.datasource.password");
            Class.forName(properties.getProperty("spring.datasource.driver-class-name"));
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            logger.error("获取数据库连接失败");
            e.printStackTrace();
        }
        return connection;
    }
}
