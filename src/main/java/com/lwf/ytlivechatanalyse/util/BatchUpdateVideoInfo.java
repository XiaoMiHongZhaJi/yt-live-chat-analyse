package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        List<String> urlList = getUrlList("@Chenyifaer288", "2023-12-30", null);
        int count = urlList.size();
        logger.info("获取到待更新url数量：" + count);
        CurlUtil.proxy = "http://127.0.0.1:7890";
        int i = 0;
        for (String url : urlList){
            Map<String, String> liveInfo = CurlUtil.getLiveInfo(url);
            updateVideoInfo(url, liveInfo);
            i ++;
            logger.info("{}/{} 已更新 {} {}", i, count, liveInfo.get("publishDate"), liveInfo.get("title"));
            Thread.sleep(1000 + (int)(Math.random() * 2 * 1000));
        }
    }

    public static void updateVideoInfo(String url, Map<String, String> liveInfo){
        String title = liveInfo.get("title");
        String likeCount = liveInfo.get("likeCount");
        String viewCount = liveInfo.get("viewCount");
        String durationTime = liveInfo.get("videoDurationTime");
        String publishDate = liveInfo.get("publishDate");
        String commentCount = liveInfo.get("commentCount");
        StringBuilder sql = new StringBuilder("update video_info set ");
        List<String> params = new ArrayList<>();
        if(StringUtils.isNotBlank(publishDate)){
            sql.append("publish_date = ?, ");
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
        sql.append("update_time = SYSDATE(), platform = 'y' ");
        sql.append("where url = ? ");
        params.add(url);
        JDBCUtil.executeUpdate(sql.toString(), params);
    }

    private static List<String> getUrlList(String start) {
        return getUrlList("@Chenyifaer288", start, null);
    }

    public static List<String> getUrlList(String authorId, String start, String end){
        String sql = "select url from video_info where 1 = 1 ";
        List<String> params = new ArrayList<>();
        if(StringUtils.isNotBlank(authorId)){
            sql += "and author_id = ? ";
            params.add(authorId);
        }
        if(StringUtils.isNotBlank(start)){
            sql += "and publish_date >= ? ";
            params.add(start);
        }
        if(StringUtils.isNotBlank(end)){
            sql += "and publish_date <= ? ";
            params.add(end);
        }
        //当天更新过的不再更新
        sql += "and (update_time is null or DATE_FORMAT(SYSDATE(), '%Y-%m-%d') != DATE_FORMAT(update_time, '%Y-%m-%d')) ";
        //更新过的不再更新
        //sql += "and update_time is null ";
        sql += "order by publish_date, id ";
        return JDBCUtil.queryStringList(sql, params);
    }
}
