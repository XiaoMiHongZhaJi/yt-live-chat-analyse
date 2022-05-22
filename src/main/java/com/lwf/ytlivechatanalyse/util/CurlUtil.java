package com.lwf.ytlivechatanalyse.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class CurlUtil {
    private static final Logger logger = LoggerFactory.getLogger(CurlUtil.class);

    public static void main(String[] args){
//        execCurl("http://www.baidu.com/", "get");
        getLiveInfo("https://www.youtube.com/watch?v=QsDsjqxDh7c");
//        List<Map<String, String>> playlist = getPlaylist("https://www.youtube.com/playlist?list=PLi3zrmUZHiY-eH8eNJiwj-viwP3ngIkcd");

    }

    public static Map<String, String> getLiveInfo(String url){
        String curl = execCurl(url);
        Map<String, String> info = new HashMap<>();
        if(StringUtils.isEmpty(curl)){
            return info;
        }
        //"topLevelButtons":[{"toggleButtonRenderer":{
        int index = curl.indexOf("topLevelButtons");
        if(index > -1){
            String curlJson = curl.substring(index + 42);
            try {
                JSONObject jsonObject = getJsonObject(curlJson);
                String likeCount = ((JSONObject) jsonObject.get("defaultText")).get("simpleText").toString();
                info.put("likeCount", likeCount);
            } catch (Exception e){
                logger.error(e.getMessage());
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
                JSONObject liveBroadcastDetails = (JSONObject) jsonObject.get("liveBroadcastDetails");
                String isLiveNow = putNotNull(info, liveBroadcastDetails, "isLiveNow");
                String startTimestamp = putNotNull(info, liveBroadcastDetails, "startTimestamp");
                String endTimestamp = putNotNull(info, liveBroadcastDetails, "endTimestamp");
                info.put("liveDate", startTimestamp.split("T")[0]);
                if("true".equals(isLiveNow)){
                    info.put("liveStatus", "1");
                }else if(StringUtils.isBlank(endTimestamp)){
                    info.put("liveStatus", "0");
                }else{
                    info.put("liveStatus", "2");
                }
            } catch (Exception e){
                logger.info(e.getMessage());
            }
        }
        logger.info(info.toString());
        return info;
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
    public static String execCurl(String url){
        String cmd = "curl -X GET " + url;
        return CmdUtil.execCmd(cmd, "UTF-8", false, true);
    }

    public static String execCurls(String... args){
        String[] cmds = {"curl", "", "-X", "GET"};
        if(args == null || args.length == 0){
            return null;
        }
        if(args.length > 0){
            cmds[1] = args[0];
        }
        if(args.length > 1){
            cmds[3] = args[1].toUpperCase();
        }
        return CmdUtil.execCmds(cmds, "UTF-8", false, true);
    }
}
