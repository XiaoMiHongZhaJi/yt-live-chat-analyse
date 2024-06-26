package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class BatchDownloadSong {

    private static final Logger logger = LoggerFactory.getLogger(BatchDownloadSong.class);

    private static final String SONG_PATH = "E:\\Music\\2024直播歌曲集\\";

    private static final String IMG_PATH = "E:\\Music\\2024直播歌曲集\\img\\";

    private static final String AUTOHR_ID = "@Chenyifaer288";
    private static final String START_DATE = "2024-02";

    public static void main(String[] args) throws Exception{

        CurlUtil.proxy = "http://127.0.0.1:7890";
        YtDlpUtil.proxy = "http://127.0.0.1:7890";
        getAndAddVideoList(AUTOHR_ID);
        List<Map<String, String>> songList = getSongList(AUTOHR_ID, START_DATE);
        int count = songList.size();
        logger.info("获取到待下载歌曲数量：" + count);
        int i = 1;
        for (Map<String, String> info : songList){
            String url = info.get("url");
            String fileName = info.get("title");
            if(StringUtils.isBlank(fileName)){
                fileName = url.substring(url.indexOf("v=") + 2);
                int index = fileName.indexOf("&");
                if(index > -1){
                    fileName = fileName.substring(0, index);
                }
            }
            fileName = fileName.replaceAll("陈一发儿|—|－|＊", "");
            String date = info.get("publish_date");
            if(StringUtils.isNotBlank(date)){
                date = date.replace("-", ".").replace("202", "2");
                fileName = date + "-" + fileName;
            }
            String file = SONG_PATH + fileName;
            File f = new File(file);
            if(f.exists()){
                logger.info(fileName + "已存在，不再下载");
                updateDownloaded(url);
                continue;
            }
            logger.info(String.format("准备下载：%s，%s/%s", fileName, i ++, count));
            String result = YtDlpUtil.downMp3(url, fileName, SONG_PATH);
            if(StringUtils.isNotBlank(result) && result.contains("Deleting original file")) {
                updateDownloaded(url);
                logger.info(fileName + " 下载成功");
            }else{
                logger.error(fileName + " 下载失败");
            }
        }
        int updateImgInfo = updateImgInfo(AUTOHR_ID, START_DATE);
        logger.info("已更新图片信息，条数：" + updateImgInfo);
        List<Map<String, String>> imgList = getImgList(AUTOHR_ID, START_DATE);
        count = imgList.size();
        logger.info("获取到待下载图片数量：" + count);
        i = 1;
        for (Map<String, String> info : imgList){
            String img = info.get("img");
            String publishDate = info.get("publish_date");
            String title = info.get("title");
            publishDate = publishDate.replace("-", ".");
            publishDate = publishDate.replace("202", "2");
            publishDate = publishDate.replace("201", "1");
            title = title.replaceAll("陈一发儿|—|－|＊", "");
            String fileName = String.format("%s-%s.jpg", publishDate, title);
            String file = IMG_PATH + fileName;
            File f = new File(file);
            if(f.exists()){
                logger.info(fileName + "已存在，不再下载");
                continue;
            }
            double length = (double) CurlUtil.downloadFile(img, fileName, IMG_PATH) / 1024;
            if(length < 5){
                img = img.replace("maxresdefault.jpg", "hqdefault.jpg");
                logger.info("下载大图失败，重新尝试小一点的图： " + img);
                length = CurlUtil.downloadFile(img, fileName, IMG_PATH) / 1024;
                if(length < 5){
                    logger.warn("下载图片失败：" + length + "KB，2秒钟后重试");
                    Thread.sleep(2000);
                    length = CurlUtil.downloadFile(img, fileName, IMG_PATH) / 1024;
                    if(length < 5){
                        logger.error("重试仍然失败：" + img);
                    }
                    continue;
                }
                logger.info("下载小一点的图完成");
            }
            logger.info("{}/{} {} 下载完成：{}KB", i ++, count, fileName, length);
        }
    }

    private static List<Map<String, String>> getImgList(String authorId, String startDate) {
        String sql = "select publish_date, img, title from video_info where 1 = 1 ";
        List<String> params = new ArrayList<>();
        if(StringUtils.isNotBlank(authorId)){
            sql += "and author_id = ? ";
            params.add(authorId);
        }
        // 过滤
        sql += "and img is not null ";
        // 2023年
        sql += "and publish_date > '2023-05' ";
        if(StringUtils.isNotBlank(startDate)){
            sql += "and publish_date > ? ";
            params.add(startDate);
        }
        sql += "order by publish_date desc";
        return JDBCUtil.queryMapList(sql, params);
    }

    private static int updateImgInfo(String authorId, String startDate) {
        String sql = "select id, publish_date, url from video_info where 1 = 1 ";
        List<String> params = new ArrayList<>();
        if(StringUtils.isNotBlank(authorId)){
            sql += "and author_id = ? ";
            params.add(authorId);
        }
        // 过滤
        sql += "and img is null ";
//        sql += "and publish_date < '2021-10-09' ";
        sql += "and url is not null ";
        if(StringUtils.isNotBlank(startDate)){
            sql += "and publish_date > ? ";
            params.add(startDate);
        }
        sql += "order by publish_date";
        int updateCount = 0;
        List<Map<String, String>> list = JDBCUtil.queryMapList(sql, params);
        for(Map<String, String> map : list){
            String id = map.get("id");
            String url = map.get("url"); //https://www.youtube.com/watch?v=Zok6m6Pcm_E
            String publishDate = map.get("publish_date");
            int index = url.indexOf("v=");
            if(index > -1){
                String vId = url.substring(url.indexOf("v=") + 2);
                index = vId.indexOf("&");
                if(index > -1) {
                    vId = vId.substring(0, index);
                }
                String img = "https://i.ytimg.com/vi/" + vId + "/maxresdefault.jpg";
                String updateSql = "update video_info set img = ? where id = ?";
                updateCount += JDBCUtil.executeUpdate(updateSql, Arrays.asList(img, id));
            }
        }
        return updateCount;
    }

    public static void getAndAddVideoList(String authorId) {
        List<Map<String, String>> videoList = getVideoList(authorId);
        int addCount = 0;
        for (Map<String, String> info : videoList){
            String publishDate = info.get("publishDate");
            if(StringUtils.length(publishDate) == 10) {
                Date date = DateUtil.parseDate(publishDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int week = calendar.get(Calendar.DAY_OF_WEEK);
                if(week == 1 || week == 3 || week == 5){
                    logger.info("{}是周{}，自动提前一天", publishDate, week - 1);
                    calendar.add(Calendar.DATE, -1);
                    date = calendar.getTime();
                    publishDate = DateUtil.getDateString(date);
                    info.put("publishDate", publishDate);
                }
            }
            addCount += addVideoInfo(info);
        }
        logger.info("获取到VideoList条数：" + videoList.size() + "，数据库新增条数：" + addCount);
    }

    private static void updateDownloaded(String url){
        String sql = "update video_info set down_song_status = '1' where url = ?";
        JDBCUtil.executeUpdate(sql, Arrays.asList(url));
    }

    private static List<Map<String, String>> getSongList(String authorId, String startDate){
        String sql = "select url, title, publish_date from video_info where 1 = 1 ";
        List<String> params = new ArrayList<>();
        if(StringUtils.isNotBlank(authorId)){
            sql += "and author_id = ? ";
            params.add(authorId);
        }
        // 排除下载过的
        sql += "and down_song_status != '1' ";
        // 2024年
        sql += "and publish_date like '2024%' ";
        sql += "and publish_date > '2023-04' ";
        if(StringUtils.isNotBlank(startDate)){
            sql += "and publish_date > ? ";
            params.add(startDate);
        }
        sql += "order by publish_date";
        return JDBCUtil.queryMapList(sql, params);
    }

    private static int addVideoInfo(Map<String, String> info){
        String sql = "insert into video_info(author_id,publish_date,url,title,img,view_count,duration_time,platform,update_time) ";
        sql += "select ?,?,?,?,?,?,?,'y',SYSDATE() from dual where not exists ";
        sql += "(select 1 from video_info where url = ? )";
        List<String> params = new ArrayList<>();
        params.add(info.get("authorId"));
        params.add(info.get("publishDate"));
        params.add(info.get("url"));
        params.add(info.get("title"));
        params.add(info.get("img"));
        params.add(info.get("viewCount"));
        params.add(info.get("lengthText"));
        params.add(info.get("url"));
        return JDBCUtil.executeUpdate(sql, params);
    }

    public static List<Map<String, String>> getVideoList(String authorId){
        String videosUrl = "https://www.youtube.com/" + authorId + "/videos";
        String curl = CurlUtil.execCurl(videosUrl);
        List<Map<String, String>> list = new ArrayList<>();
        //"videoRenderer"
        int index = curl.indexOf("videoRenderer");
        while (index > -1) {
            curl = curl.substring(index + 13);
            index = curl.indexOf("videoRenderer");
            String content = null;
            if(index > -1){
                content = curl.substring(0, index);
            }else {
                content = curl;
            }
            try {
                Map<String, String> info = new HashMap<>();

                String img = CurlUtil.getJsonValue(content, "thumbnails", 100, "url", "?");
                info.put("img", img);

                String title = CurlUtil.getJsonValue(content, "title", 100, "text");
                info.put("title", title);

                String publishedTime = CurlUtil.getJsonValue(content, "publishedTimeText", 50);
                info.put("publishedTime", publishedTime);
                if(StringUtils.isNotBlank(publishedTime)){
                    String publishDate = DateUtil.getDayBefore(publishedTime);
                    info.put("publishDate", publishDate);
                }

                String lengthText = CurlUtil.getJsonValue(content, "lengthText", 250);
                info.put("lengthText", lengthText);

                String viewCount = CurlUtil.getJsonValue(content, "viewCountText", 50, "simpleText", "次观看");
                if(StringUtils.isNotBlank(viewCount)){
                    viewCount = viewCount.replace(",","");
                }
                info.put("viewCount", viewCount);

                String url = CurlUtil.getJsonValue(content, "webCommandMetadata", 350, "url", ",");
                info.put("url", "https://www.youtube.com" + url);

                info.put("authorId", authorId);
                list.add(info);
            }catch (Exception e){
                logger.error("获取videoInfo失败", e);
            }
        }
        return list;
    }
}
