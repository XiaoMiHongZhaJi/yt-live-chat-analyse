package com.lwf.ytlivechatanalyse.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class CurlUtil {
    private static final Logger logger = LoggerFactory.getLogger(CurlUtil.class);

    public static void main(String[] args) {
//        execCurl("http://www.baidu.com/", "get");
        CurlUtil.proxy = "http://192.168.10.30:7890";
        CurlUtil.cookie = "C:\\Users\\Administrator\\IdeaProjects\\yt-live-chat-analyse\\cookies.txt";
        CurlUtil.curlLog = "C:\\Users\\Administrator\\IdeaProjects\\yt-live-chat-analyse\\html\\";
        Map<String, String> liveInfo = getLiveInfo("https://www.youtube.com/watch?v=ImR8jZ0SkRM");
        System.out.println(liveInfo);
//        List<Map<String, String>> playlist = getPlaylist("https://www.youtube.com/playlist?list=PLi3zrmUZHiY-eH8eNJiwj-viwP3ngIkcd");
//        System.out.println(playlist);
    }

    public static String proxy;

    @Value("${proxy}")
    public void setProxy(String proxy) {
        CurlUtil.proxy = proxy;
    }

    public static String cookie;

    @Value("${cookie}")
    public void setCookie(String cookie) {
        CurlUtil.cookie = cookie;
    }

    public static String curlLog;

    @Value("${curlLog}")
    public void setCurlLog(String curlLog) {
        CurlUtil.curlLog = curlLog;
    }

    public static Map<String, String> getLiveInfo(String url, String cookieOuter) {
        if(StringUtils.isNotBlank(cookie)){
            File cookieFile = new File(cookie);
            if (!cookieFile.exists()) {
                try {
                    cookieFile.createNewFile();
                } catch (Exception e) {
                    logger.error("创建cookieFile失败", e);
                }
            }
            if (StringUtils.isNotBlank(cookieOuter)) {
                if (cookieOuter.startsWith("# Netscape HTTP Cookie File")) {
                    FileUtil.writeAsString(cookieFile, cookieOuter);
                }
            }
        }
        return getLiveInfo(url);
    }

    public static Map<String, String> getLiveInfo(String url) {
        String curl = execCurl(url);
        if(StringUtils.isNotBlank(curlLog)){
            try {
                File curlLogPath = new File(curlLog);
                if (!curlLogPath.exists()) {
                    curlLogPath.mkdirs();
                }
                String curlLogName = url.split("v=")[1] + "_" + DateUtil.getNowDateTime().replace(":", "_");
                String curlLogFile = String.format("%s%s.html", curlLog, curlLogName);
                FileUtil.writeAsString(new File(curlLogFile), curl);
                logger.info(String.format("curlLog已写入：%s", curlLogFile));
            } catch (Exception e) {
                logger.error("写入curlLog失败", e);
            }
        }
        /*String curl = "";
        try {
            curl = FileUtil.readAsString(new File("C:\\Users\\Administrator\\IdeaProjects\\yt-live-chat-analyse\\aa.html"));
        }catch (Exception e){}*/
        Map<String, String> info = new HashMap<>();
        if (StringUtils.isEmpty(curl)) {
            return info;
        }
        //{"iconName":"LIKE","title":"17",
        int index = curl.indexOf("\"LIKE\",\"title\"");
        if (index > -1) {
            try {
                String like = curl.substring(index + 14, index + 30);
                String likeCount = like.substring(like.indexOf(":\"") + 2, like.indexOf("\","));
                info.put("likeCount", likeCount);
            } catch (Exception e) {
                logger.error("获取likeCount信息出错", e);
            }
        }
        //"videoPrimaryInfoRenderer":{
        index = curl.indexOf("videoPrimaryInfoRenderer");
        if (index > -1) {
            String curlJson = curl.substring(index + 26);
            try {
                JSONObject jsonObject = getJsonObject(curlJson);
                putNotNull(info, jsonObject.getJSONObject("title")
                        .getJSONArray("runs")
                        .getJSONObject(0), "text", "title");
                putNotNull(info, jsonObject.getJSONObject("viewCount")
                        .getJSONObject("videoViewCountRenderer"), "originalViewCount", "livingViewCount");
                String livingViewCount = info.get("livingViewCount");
                info.put("viewCount", livingViewCount);
                if ("0".equals(livingViewCount)) {
                    String viewCount = jsonObject.getJSONObject("viewCount").getJSONObject("videoViewCountRenderer").getJSONObject("viewCount").getString("simpleText");
                    viewCount = viewCount.replaceAll("次观看|views|,| ", "");
                    info.put("viewCount", viewCount);
                }
            } catch (Exception e) {
                logger.error("获取liveInfo信息出错", e);
            }
        }
        //需要cookie "playerMicroformatRenderer":{
        index = curl.indexOf("playerMicroformatRenderer");
        if (index > -1) {
            String curlJson = curl.substring(index + 27);
            try {
                JSONObject jsonObject = getJsonObject(curlJson);
                putNotNull(info, (JSONObject) jsonObject.get("title"), "simpleText", "title");
                putNotNull(info, jsonObject, "viewCount");
                putNotNull(info, jsonObject, "uploadDate");
                putNotNull(info, jsonObject, "publishDate");
                info.put("publishDate", info.get("publishDate").substring(0, 10));
                JSONObject liveBroadcastDetails = (JSONObject) jsonObject.get("liveBroadcastDetails");
                if (liveBroadcastDetails != null) {
                    String isLiveNow = putNotNull(info, liveBroadcastDetails, "isLiveNow");
                    String startTime = putNotNull(info, liveBroadcastDetails, "startTimestamp", "startTime");
                    String endTime = putNotNull(info, liveBroadcastDetails, "endTimestamp", "endTime");
                    info.put("liveDate", startTime.split("T")[0]);
                    if ("true".equals(isLiveNow)) {
                        info.put("liveStatus", LiveInfo.LIVE_STATUS_LIVEING);
                    } else if (StringUtils.isBlank(endTime)) {
                        info.put("liveStatus", LiveInfo.LIVE_STATUS_PREVIEW);
                    } else {
                        info.put("liveStatus", LiveInfo.LIVE_STATUS_DONE);
                    }
                    if (StringUtils.isNotBlank(startTime)) {
                        info.put("startTimestamp", String.valueOf(DateUtil.getTimestamp(startTime)));
                    }
                }
            } catch (Exception e) {
                logger.error("获取liveInfo信息出错", e);
            }
        } else if(curl.contains("\"publishDate\":{\"simpleText\"")){
            logger.warn("cookie已过期");
            info.put("errInfo", "cookie已过期");
            //"publishDate":{"simpleText":"直播开始日期：2024年8月14日"}
            String publishDateString = getJsonValue(curl, "publishDate", 60);
            String publishDate = publishDateString.substring(publishDateString.indexOf("：") + 1);
            if (publishDateString.contains("预定发布时间")) {
                //预定发布时间：2024年8月17日
                info.put("liveStatus", LiveInfo.LIVE_STATUS_PREVIEW);
                publishDate = DateUtil.converDateStringCN(publishDate);
            } else if (publishDateString.contains("直播开始时间")) {
                //直播开始时间：4小时前
                info.put("liveStatus", LiveInfo.LIVE_STATUS_LIVEING);
                publishDate = DateUtil.getDayBefore(publishDate);
            } else if (publishDateString.contains("上次直播时间")) {
                //上次直播时间：6小时前
                info.put("liveStatus", LiveInfo.LIVE_STATUS_DONE);
                publishDate = DateUtil.getDayBefore(publishDate);
            } else if (publishDateString.contains("直播开始日期")) {
                //直播开始日期：2024年8月14日
                info.put("liveStatus", LiveInfo.LIVE_STATUS_DONE);
                publishDate = DateUtil.converDateStringCN(publishDate);
            }
            info.put("publishDate", publishDate);
        }
        //"commentCount":{"simpleText":"24"}
        String commentCount = getJsonValue(curl, "commentCount", 40);
        info.put("commentCount", commentCount);
        logger.info(info.toString());
        return info;
    }

    public static String getJsonValue(String content, String columeName, int length, String key, String endWord) {
        int index = content.indexOf(columeName);
        if (index <= -1) {
            return "";
        }
        String text = content.substring(index + columeName.length(), index + length);
        text = text.substring(text.indexOf(key));
        text = text.substring(key.length(), text.indexOf(endWord));
        int i = text.indexOf(":");
        if (i > -1) {
            text = text.substring(i + 1);
        }
        text = text.replaceAll("[\\s\"{}]", "");
        return text;
    }

    public static String getJsonValue(String content, String columeName, int length, String key) {
        return getJsonValue(content, columeName, length, key, "}");
    }

    public static String getJsonValue(String content, String columeName, int length) {
        return getJsonValue(content, columeName, length, "simpleText", "}");
    }

    private static String putNotNull(Map<String, String> info, JSONObject jsonObject, String key, String infoKey) {
        Object obj = jsonObject.get(key);
        if (obj instanceof String) {
            String value = jsonObject.getString(key);
            info.put(infoKey, value);
            return value;
        }
        return "";
    }

    private static String putNotNull(Map<String, String> info, JSONObject jsonObject, String key) {
        return putNotNull(info, jsonObject, key, key);
    }

    public static List<Map<String, String>> getPlaylist(String url) {
        String curl = execCurl(url);
        String curlJson = curl.substring(curl.indexOf("playlistVideoRenderer") - 3);
        JSONArray jsonArray = getJsonArray(curlJson);
        List<Map<String, String>> itemList = new ArrayList<>();
        for (Object object : jsonArray) {
            Map<String, String> item = new HashMap<>();
            JSONObject jsonObject = (JSONObject) object;
            JSONObject playlistVideoRenderer = (JSONObject) jsonObject.get("playlistVideoRenderer");
            String videoId = playlistVideoRenderer.get("videoId").toString();
            String title = ((JSONObject) ((JSONArray) ((JSONObject) playlistVideoRenderer.get("title")).get("runs")).get(0)).get("text").toString();
            String durationTime = null;
            if (playlistVideoRenderer.get("lengthText") != null) {
                durationTime = ((JSONObject) playlistVideoRenderer.get("lengthText")).get("simpleText").toString();
            }
            item.put("title", title);
            item.put("videoId", videoId);
            item.put("durationTime", durationTime);
            item.put("img", "https://i.ytimg.com/vi/" + videoId + "/hqdefault.jpg");
            item.put("url", "https://www.youtube.com/watch?v=" + videoId);
            itemList.add(item);
        }
        return itemList;
    }

    private static JSONObject getJsonObject(String curlJson) {
        char[] charArray = curlJson.toCharArray();
        int offset = 0;
        int count = 0;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
            }
            if (count == 0) {
                offset = i;
                break;
            }
        }
        String json = curlJson.substring(0, offset + 1);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject;
    }

    private static JSONArray getJsonArray(String curlJson) {
        char[] charArray = curlJson.toCharArray();
        int offset = 0;
        int count = 0;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '[') {
                count++;
            } else if (c == ']') {
                count--;
            }
            if (count == 0) {
                offset = i;
                break;
            }
        }
        String json = curlJson.substring(0, offset + 1);
        JSONArray jsonArray = JSON.parseArray(json);
        return jsonArray;
    }

    public static long downloadFile(String url, String fileName, String filePath) {
        List<String> cmd = new ArrayList<>();
        cmd.add("curl");
        cmd.add("-H");
        cmd.add("accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7'");
        cmd.add("-H");
        cmd.add("accept-language: zh-CN,zh;q=0.9");
        cmd.add("-H");
        cmd.add("user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        if (StringUtils.isNotBlank(proxy)) {
            cmd.add("--proxy");
            cmd.add(proxy);
        }
        if (StringUtils.isNotBlank(fileName)) {
            if (StringUtils.isNotBlank(filePath)) {
                File path = new File(filePath);
                if (!path.exists()) {
                    path.mkdirs();
                }
                fileName = filePath + fileName;
            }
            cmd.add("-o");
            cmd.add(fileName);
        } else {
            cmd.add("-O");
        }
        cmd.add(url);
        CmdUtil.execCmd(cmd.toArray(new String[0]), false, true);
        if (StringUtils.isNotBlank(fileName)) {
            File f = new File(fileName);
            return f.length();
        }
        return 0;
    }

    public static String execCurl(String url) {
        List<String> cmd = new ArrayList<>();
        cmd.add("curl");
        cmd.add("-H");
        cmd.add("'accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7'");
        cmd.add("-H");
        cmd.add("'accept-language: zh-CN,zh;q=0.9'");
        cmd.add("-H");
        cmd.add("'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36'");
        if (StringUtils.isNotBlank(proxy)) {
            cmd.add("--proxy");
            cmd.add(proxy);
        }
        if (StringUtils.isNotBlank(cookie)) {
            cmd.add("-b");
            cmd.add(cookie);
            cmd.add("-c");
            cmd.add(cookie);
        }
        cmd.add(url);
        return CmdUtil.execCmd(cmd.toArray(new String[0]), false, true, "UTF-8");
    }
}
