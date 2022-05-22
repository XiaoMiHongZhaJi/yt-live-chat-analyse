package com.lwf.ytlivechatanalyse.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtil {

    /**
     * 从消息中提取emoji
     * @param message
     * @return
     */
    public static Set<String> getEmojiKey(String message){
        Matcher matcher = Pattern.compile("\\:\\w+\\:").matcher(message);
        Set<String> stringSet = new HashSet<>();
        while (matcher.find()){
            String emoji = matcher.group();
            stringSet.add(emoji);
        }
        return stringSet;
    }

    /**
     * jsonObj转LiveChatDto
     * @param jsonObject
     * @return
     */
    public static LiveChatData getLiveChat(JSONObject jsonObject){
        LiveChatData dto = new LiveChatData();
        dto.setTimeText((String)jsonObject.get("time_text"));
        dto.setTimestamp((Long) jsonObject.get("timestamp"));
        Object time_in_seconds = jsonObject.get("time_in_seconds");
        if(time_in_seconds instanceof BigDecimal){
            dto.setTimeInSeconds((BigDecimal) time_in_seconds);
        }else if(time_in_seconds != null){
            dto.setTimeInSeconds(new BigDecimal(time_in_seconds.toString()));
        }
        JSONObject author = (JSONObject) jsonObject.get("author");
        try {
            //用户名 和 消息，可能会抛出异常
            String name = (String) author.get("name");
            String display_name = (String) author.get("display_name");
            dto.setAuthorName(StringUtils.isBlank(display_name) ? name : display_name);
            dto.setAuthorId((String) author.get("id"));
            JSONArray images = (JSONArray) author.get("images");
            if(images != null && images.size() > 0){
                dto.setAuthorImage((String)((JSONObject)images.get(0)).get("url"));
            }
            String message = (String) jsonObject.get("message");
            if (message.length() > 600){
                message = message.substring(0,600);
            }
            dto.setMessage(message);
        }catch (Exception e){
        }
        return dto;
    }

    /**
     * jsonObj转Emotes
     * @param emotes
     * @return
     */
    public static List<EmotesData> getEmotes(JSONArray emotes){
        List<EmotesData> emotesDataList = new ArrayList<>();
        for (Object emote : emotes){
            EmotesData emotesData = new EmotesData();
            JSONObject emotesJsonObject = (JSONObject) emote;
            String name = (String) emotesJsonObject.get("name");
            emotesData.setName(name.replace(":",""));
            emotesData.setEmotesId((String) emotesJsonObject.get("id"));
            emotesData.setIsCustomEmoji((Boolean) emotesJsonObject.get("is_custom_emoji"));
            JSONArray images = (JSONArray) emotesJsonObject.get("images");
            if(images != null && images.size() > 0){
                emotesData.setImages((String)((JSONObject)images.get(0)).get("url"));
            }
            emotesDataList.add(emotesData);
        }
        return emotesDataList;
    }
}
