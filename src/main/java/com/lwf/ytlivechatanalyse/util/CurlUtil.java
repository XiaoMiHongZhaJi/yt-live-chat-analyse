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

    public static void main(String[] args){
//        execCurl("http://www.baidu.com/", "get");
        CurlUtil.proxy = "http://127.0.0.1:7890";
        Map<String, String> liveInfo = getLiveInfo("https://www.youtube.com/watch?v=KlGA3S6YZn8\n");
        System.out.println(liveInfo);
//        List<Map<String, String>> playlist = getPlaylist("https://www.youtube.com/playlist?list=PLi3zrmUZHiY-eH8eNJiwj-viwP3ngIkcd");
//        System.out.println(playlist);
    }

    public static String proxy;

    @Value("${proxy}")
    public void setProxy(String proxy) {
        CurlUtil.proxy = proxy;
    }

    public static Map<String, String> getLiveInfo(String url){
        String curl = execCurl(url);
//        String curl = "";
//        try {
//            curl = FileUtil.readAsString(new File("D:\\Documents\\IdeaProjects\\yt-live-chat-analyse\\test\\arm_curl.html"));
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        Map<String, String> info = new HashMap<>();
        if(StringUtils.isEmpty(curl)){
            return info;
        }
        //{"iconName":"LIKE","title":"17",
        int index = curl.indexOf("\"LIKE\",\"title\"");
        if(index > -1){
            try {
                String like = curl.substring(index + 14, index + 30);
                String likeCount = like.substring(like.indexOf(":\"") + 2, like.indexOf("\","));
                info.put("likeCount", likeCount);
            } catch (Exception e){
                logger.error("获取likeCount信息出错", e);
            }
        }
        //"playerMicroformatRenderer":{
        index = curl.indexOf("playerMicroformatRenderer");
        if(index > -1){
            String curlJson = curl.substring(index + 27);
            try {
                JSONObject jsonObject = getJsonObject(curlJson);
                putNotNull(info, (JSONObject) jsonObject.get("title"), "simpleText", "title");
                putNotNull(info, jsonObject, "viewCount");
                putNotNull(info, jsonObject, "uploadDate");
                putNotNull(info, jsonObject, "publishDate");
                info.put("publishDate", info.get("publishDate").substring(0, 10));
                JSONObject liveBroadcastDetails = (JSONObject) jsonObject.get("liveBroadcastDetails");
                if(liveBroadcastDetails != null){
                    String isLiveNow = putNotNull(info, liveBroadcastDetails, "isLiveNow");
                    String startTime = putNotNull(info, liveBroadcastDetails, "startTimestamp", "startTime");
                    String endTime = putNotNull(info, liveBroadcastDetails, "endTimestamp", "endTime");
                    info.put("liveDate", startTime.split("T")[0]);
                    if("true".equals(isLiveNow)){
                        info.put("liveStatus", LiveInfo.LIVE_STATUS_LIVEING);
                    }else if(StringUtils.isBlank(endTime)){
                        info.put("liveStatus", LiveInfo.LIVE_STATUS_PREVIEW);
                    }else{
                        info.put("liveStatus", LiveInfo.LIVE_STATUS_DONE);
                    }
                    if(StringUtils.isNotBlank(startTime)){
                        info.put("startTimestamp", String.valueOf(DateUtil.getTimestamp(startTime)));
                    }
                }
            } catch (Exception e){
                logger.error("获取liveInfo信息出错", e);
            }
        }
        //"approxDurationMs": "697991",
        index = curl.indexOf("approxDurationMs");
        if(index > -1){
            String duration = curl.substring(index + 17, index + 30);
            duration = duration.split(",")[0];
            duration = duration.replaceAll("[\\s:\"]", "");
            if(StringUtils.isNumeric(duration)){
                String time = DateUtil.secondToString(Integer.parseInt(duration) / 1000);
                info.put("durationTime", time);
            }
        }
        //"videoViewCountRenderer": {"viewCount": {"runs": [{"text": "1,983"}, {"text": " 人正在观看"}]},"isLive": true} 直播中
        //"videoViewCountRenderer":{"viewCount":{"simpleText":"26,517次观看"}," 直播结束
        //"videoViewCountRenderer":{"viewCount":{"simpleText":"23,960 views"}
        index = curl.indexOf("videoViewCountRenderer");
        if(index > -1){
            String livingViewCount = curl.substring(index + 47, index + 70);
            livingViewCount = livingViewCount.substring(livingViewCount.indexOf("text") + 4, livingViewCount.indexOf("}"));
            livingViewCount = livingViewCount.replaceAll("[\\s:\",]", "");
            if(StringUtils.isNumeric(livingViewCount)){
                info.put("livingViewCount", livingViewCount);
            }
        }
        //"commentCount": {"simpleText": "926"},
        String commentCount = getJsonValue(curl, "commentCount", 40);
        info.put("commentCount", commentCount);
        logger.info(info.toString());
        return info;
    }

    public static String getJsonValue(String content, String columeName, int length, String key, String endWord) {
        int index = content.indexOf(columeName);
        if(index <= -1){
            return "";
        }
        String text = content.substring(index + columeName.length(), index + length);
        text = text.substring(text.indexOf(key));
        text = text.substring(key.length(), text.indexOf(endWord));
        int i = text.indexOf(":");
        if(i > -1){
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

    private static String putNotNull(Map<String, String> info, JSONObject jsonObject, String key, String infoKey){
        Object obj = jsonObject.get(key);
        if(obj != null){
            String value = jsonObject.get(key).toString();
            info.put(infoKey, value);
            return value;
        }
        return "";
    }
    private static String putNotNull(Map<String, String> info, JSONObject jsonObject, String key){
        return putNotNull(info, jsonObject, key, key);
    }

    public static List<Map<String, String>> getPlaylist(String url){
        String curl = execCurl(url);
        String curlJson = curl.substring(curl.indexOf("playlistVideoRenderer") - 3);
        JSONArray jsonArray = getJsonArray(curlJson);
        List<Map<String, String>> itemList = new ArrayList<>();
        for (Object object : jsonArray){
            Map<String, String> item = new HashMap<>();
            JSONObject jsonObject = (JSONObject) object;
            JSONObject playlistVideoRenderer = (JSONObject) jsonObject.get("playlistVideoRenderer");
            String videoId = playlistVideoRenderer.get("videoId").toString();
            String title = ((JSONObject)((JSONArray)((JSONObject) playlistVideoRenderer.get("title")).get("runs")).get(0)).get("text").toString();
            String durationTime = null;
            if(playlistVideoRenderer.get("lengthText") != null){
                durationTime = ((JSONObject) playlistVideoRenderer.get("lengthText")).get("simpleText").toString();
            }
            item.put("title", title);
            item.put("videoId", videoId);
            item.put("durationTime", durationTime);
            item.put("img", "https://i.ytimg.com/vi/"+ videoId + "/hqdefault.jpg");
            item.put("url", "https://www.youtube.com/watch?v="+ videoId);
            itemList.add(item);
        }
        return itemList;
    }

    private static JSONObject getJsonObject(String curlJson){
        char[] charArray = curlJson.toCharArray();
        int offset = 0;
        int count = 0;
        for (int i = 0; i < charArray.length; i++){
            char c = charArray[i];
            if(c == '{'){
                count ++;
            }else if(c == '}'){
                count --;
            }
            if(count == 0){
                offset = i;
                break;
            }
        }
        String json = curlJson.substring(0, offset + 1);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject;
    }

    private static JSONArray getJsonArray(String curlJson){
        char[] charArray = curlJson.toCharArray();
        int offset = 0;
        int count = 0;
        for (int i = 0; i < charArray.length; i++){
            char c = charArray[i];
            if(c == '['){
                count ++;
            }else if(c == ']'){
                count --;
            }
            if(count == 0){
                offset = i;
                break;
            }
        }
        String json = curlJson.substring(0, offset + 1);
        JSONArray jsonArray = JSON.parseArray(json);
        return jsonArray;
    }

    public static long downloadFile(String url, String fileName, String filePath){
        String cmd = "curl ";
        if(StringUtils.isNotBlank(proxy)){
            cmd += "--proxy " + proxy + " ";
        }
        if(StringUtils.isNotBlank(fileName)){
            if(StringUtils.isNotBlank(filePath)){
                File path = new File(filePath);
                if(!path.exists()){
                    path.mkdirs();
                }
                fileName = filePath + fileName;
            }
            cmd += "-o \"" + fileName + "\" ";
        }else{
            cmd += "-O ";
        }
        CmdUtil.execCmd(cmd + url, false, true, "UTF-8");
        if(StringUtils.isNotBlank(fileName)){
            File f = new File(fileName);
            return f.length();
        }
        return 0;
    }

    public static String execCurl(String url){
        String cmd = "curl --header \"accept-language: zh-CN,zh;q=0.9\" -X GET ";
        if(StringUtils.isNotBlank(proxy)){
            cmd += "--proxy " + proxy + " ";
        }
        return CmdUtil.execCmd(cmd + url, false, true, "UTF-8");
    }
}
