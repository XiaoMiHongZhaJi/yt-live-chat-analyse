package com.lwf.ytlivechatanalyse.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.service.EmotesDataService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BulletXmlUtil {

    private static final Logger logger = LoggerFactory.getLogger(BulletXmlUtil.class);

    private static final String TYPE_SCROLL = "1"; //滚动弹幕
    private static final String TYPE_BOTTOM = "4"; //底部弹幕
    private static final String TYPE_TOP = "5"; //顶部弹幕

    private static final String FONT_SIZE = "15"; //字体大小

    private static final String FONT_COLOR_WHITE = "16777215"; //白色
    private static final String FONT_COLOR_RED = "16711680"; //红色
    private static final String FONT_COLOR_GRAY = "8421504"; //灰色
    private static final String FONT_COLOR_BLUE = "6194417"; //蓝色

    private static final Map<String, String> specType = getSpecType();

    private static EmotesDataService emotesDataService;

    private static Map<String, String> emotesMap;

    @Autowired
    public void setEmotesDataService(EmotesDataService emotesDataService) {
        BulletXmlUtil.emotesDataService = emotesDataService;
    }

    public static void main(String[] args) {
//        String liveDate = "2023-05-06";
//        Long startTimestamp = getStartTimestamp(liveDate);
//        List<LiveChatData> chatList = getChatList(liveDate);
//        initEmoteMapLocal();
//        File xmlFile = getXmlFile(chatList, startTimestamp, "E:\\67373\\直播录像\\", 0, 15);
//        initEmoteDB();
        initEmoteDB2();
    }

    /**
     * 生成xml弹幕文件
     */
    public static File getXmlFile(List<LiveChatData> chatList, Long startTimestamp, Integer block, Integer duringSecond) {
        Long minTimestamp = chatList.get(0).getTimestamp();
        Long maxTimestamp = chatList.get(chatList.size() - 1).getTimestamp();
        if(startTimestamp == null || startTimestamp < minTimestamp){
            startTimestamp = minTimestamp;
        }else if(startTimestamp > maxTimestamp){
            startTimestamp = maxTimestamp;
        }
        BufferedWriter writer = null;
        String filePath = "bullet/xml";
        File floder = new File(filePath);
        if(!floder.exists()){
            floder.mkdir();
        }
        filePath = floder.getAbsolutePath();
        if(filePath.contains("\\")){
            filePath = filePath + "\\";
        }
        if(filePath.contains("/")){
            filePath = filePath + "/";
        }
        String fileName = DateUtil.getDateTime(startTimestamp);
        File file = new File(filePath + fileName + ".xml");
        if(file.exists()){
            return file;
        }
        try{
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.newLine();
            writer.write("<i>");
            writer.newLine();
            //<d p="1568.69900,1,25,16777215,1527417135,0,96de7b6f,144158263607298">熊半城</d>
            for(LiveChatData liveChatData : chatList){
                String line = getBulletXmlLine(startTimestamp, liveChatData, block, duringSecond);
                if(line == null){
                    continue;
                }
                writer.write(line);
                writer.newLine();
            }
            writer.write("</i>");
            writer.newLine();
            writer.flush();
        }catch(Exception e){
            logger.error("生成xml文件失败", e);
        }finally{
            try {
                if(writer != null){
                    writer.close();
                }
            }catch(IOException e){
                logger.error("关闭流失败", e);
            }
        }
        return file;
    }

    private static String getBulletXmlLine(Long startTimestamp, LiveChatData liveChatData, Integer block, Integer duringSecond) {
        Long timestamp = liveChatData.getTimestamp();
        long time = timestamp - startTimestamp;
        double second = (double) time / 1000000;
        if(second < -30){
            return null;
        }
        if(second < 0){
            second = 0;
        }else if(block != null && duringSecond != null && block > 0 && duringSecond > 0){
            Integer blockSecond = duringSecond / block;
            second = (int)(second / blockSecond) * blockSecond;
        }
        String authorName = liveChatData.getAuthorName().replaceAll("[&<>\u0000-\u0019]","_");
        String message = liveChatData.getMessage();
        if(StringUtils.isBlank(message)){
            message = "";
        }else{
            message = message.replaceAll("[&<>\u0000-\u0019]","_");
            Integer emotesCount = liveChatData.getEmotesCount();
            if(emotesCount != null && emotesCount > 0){
                message = getEmoteMssage(message);
            }
            message = "：" + message;
        }
        String type = TYPE_SCROLL;
        String fontColor = FONT_COLOR_WHITE;
        String spec = specType.get(authorName);
        if(spec != null){
            type = spec;
            if(BulletXmlUtil.TYPE_BOTTOM.equals(type)){
                //房管
                fontColor = FONT_COLOR_BLUE;
            }
        }
        String scAmount = liveChatData.getScAmount();
        if(StringUtils.isNotBlank(scAmount)){
            authorName = String.format("(%s)%s", scAmount, authorName);
            fontColor = FONT_COLOR_BLUE;
            String scInfo = liveChatData.getScInfo();
            if(StringUtils.isNotBlank(scInfo)){
                int index = scInfo.indexOf("#");
                if(index > -1){
                    String colorCode = scInfo.substring(index + 1, index + 7);
                    fontColor = String.valueOf(Integer.parseInt(colorCode, 16));
                }
            }
        }
        return String.format("<d p=\"%s,%s,%s,%s,0\">%s%s</d>", second, type, FONT_SIZE, fontColor, authorName, message);
    }

    private static String getEmoteMssage(String message) {
        int index = message.indexOf(":");
        if(index > -1){
            //YouTube
            StringBuilder mewMessage = new StringBuilder(message.substring(0, index));
            String remain = message.substring(index);
            int endIndex = remain.indexOf(":", 1);
            while(endIndex > 2){
                String key = remain.substring(1, endIndex);
                if(emotesMap == null){
                    initEmoteMap();
                }
                String emote = emotesMap.get(key);
                if(StringUtils.isBlank(emote)){
                    mewMessage.append(String.format("[%s]", key));
                }else{
                    mewMessage.append(emote);
                }
                remain = remain.substring(endIndex + 1);
                int startIndex = remain.indexOf(":");
                if(startIndex > 0){
                    mewMessage.append(remain, 0, startIndex);
                    remain = remain.substring(startIndex);
                }
                endIndex = remain.indexOf(":", 1);
            }
            mewMessage.append(remain);
            return mewMessage.toString();
        }
        return message;
    }

    private static void initEmoteMap() {
        List<EmotesData> emotesData = emotesDataService.selectEmoji();
        emotesMap = new HashMap<>();
        for(EmotesData emote : emotesData){
            emotesMap.put(emote.getName(), emote.getEmotesId());
        }
    }


    private static Long getStartTimestamp(String liveDate){
        String sql = String.format("select start_timestamp from live_info where live_date = '%s' ", liveDate);
        String timestamp = JDBCUtil.queryString(sql);
        if(StringUtils.isNotBlank(timestamp)){
            return Long.valueOf(timestamp);
        }
        return null;
    }

    private static List<LiveChatData> getChatList(String liveDate){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<LiveChatData> chatList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("select author_name, sc_info, sc_amount, message, timestamp, emotes_count ");
        sql.append("  from live_chat_data where live_date like concat(?,'%') ");
        sql.append(" order by timestamp");
        try {
            conn = JDBCUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setString(1, liveDate);
            rs = ps.executeQuery();
            while(rs.next()){
                LiveChatData liveChatData = new LiveChatData();
                liveChatData.setAuthorName(rs.getString("author_name"));
                liveChatData.setMessage(rs.getString("message"));
                liveChatData.setTimestamp(rs.getLong("timestamp"));
                liveChatData.setScInfo(rs.getString("sc_info"));
                liveChatData.setScAmount(rs.getString("sc_amount"));
                liveChatData.setEmotesCount(rs.getInt("emotes_count"));
                chatList.add(liveChatData);
            }
            JDBCUtil.closeStatement(ps, rs);
        }catch (Exception e){
            logger.error("sql执行失败", e);
        }finally {
            JDBCUtil.closeConnection(conn, ps, rs);
        }
        return chatList;
    }

    private static void initEmoteMapLocal() {
        String sql = "select name, emotes_id from emotes_data where name is not null and images like '%youtube%' ";
        List<Map<String, String>> list = JDBCUtil.queryMapList(sql, null);
        emotesMap = new HashMap<>();
        for(Map<String, String> map : list){
            emotesMap.put(map.get("name"), map.get("emotes_id"));
        }
    }

    private static Map<String, String> getSpecType() {
        Map<String, String> map = new HashMap<>();
        map.put("瀧角散", TYPE_TOP);
        map.put("头号发吹", TYPE_TOP);
        map.put("小米轰炸姬", TYPE_TOP);
        map.put("可乐加味精", TYPE_TOP);
        map.put("娜边的发发", TYPE_TOP);
        map.put("萌萌發", TYPE_TOP);
        map.put("阿銀立", TYPE_TOP);
        map.put("阿銀", TYPE_TOP);
        map.put("一只偷子", TYPE_TOP);
        map.put("赵迪奥", TYPE_TOP);
        map.put("台主真是太强乐", TYPE_TOP);
        map.put("robin罗宾", TYPE_TOP);
        map.put("放映室检票员", TYPE_TOP);
        map.put("村西透", TYPE_TOP);
        map.put("旅行_", TYPE_TOP);
        map.put("旅行_带着王总去旅行", TYPE_TOP);
        map.put("带着小德去旅行", TYPE_TOP);
        map.put("呱呱", TYPE_BOTTOM);
        map.put("呱呱盒子", TYPE_BOTTOM);
        map.put("拥抱", TYPE_BOTTOM);
        map.put("萝菽菽", TYPE_BOTTOM);
        map.put("seaLLee", TYPE_BOTTOM);
        map.put("陈一发儿", TYPE_BOTTOM);
        return map;
    }

    /**
     * 初始化youtube表情符号，只需要执行一次
     */
    public static void initEmoteDB() {
        String jsonString = CurlUtil.execCurl("https://www.gstatic.com/youtube/img/emojis/emojis-svg-9.json");
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        for (Object object : jsonArray){
            try {
                JSONObject jsonObject = (JSONObject) object;
                String emojiId = (String)jsonObject.get("emojiId");
                String url = (String)((JSONObject)((JSONArray)((JSONObject)jsonObject.get("image")).get("thumbnails")).get(0)).get("url");
                JSONArray shortcuts = (JSONArray)jsonObject.get("shortcuts");
                String name = "";
                if(shortcuts == null || shortcuts.size() == 0){
                    System.out.println(object);
                }else{
                    name = (String)shortcuts.get(0);
                    name = name.replace(":", "");
                }
                String sql = String.format("insert into emotes_data(emotes_id, images, name, is_custom_emoji) values('%s','%s','%s', 0)", emojiId, url, name);
                JDBCUtil.executeUpdate(sql);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(object);
            }
        }
    }

    /**
     * 初始化youtube自定义表情符号，只需要执行一次
     */
    public static void initEmoteDB2() {
        String jsonString;
        try {
            jsonString = FileUtil.readAsString(new File("D:\\Documents\\IdeaProjects\\yt-live-chat-analyse\\src\\main\\java\\com\\lwf\\ytlivechatanalyse\\util\\customEmoji.json"));
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        for (Object object : jsonArray){
            try {
                JSONObject jsonObject = (JSONObject) object;
                String emojiId = (String)jsonObject.get("emojiId");
                String url = (String)((JSONObject)((JSONArray)((JSONObject)jsonObject.get("image")).get("thumbnails")).get(0)).get("url");
                JSONArray shortcuts = (JSONArray)jsonObject.get("shortcuts");
                String name = "";
                if(shortcuts == null || shortcuts.size() == 0){
                    System.out.println(object);
                }else{
                    name = (String)shortcuts.get(0);
                    name = name.replace(":", "");
                }
                String sql = String.format("insert into emotes_data(emotes_id, images, name, is_custom_emoji) values('%s','%s','%s', 1)", emojiId, url, name);
                JDBCUtil.executeUpdate(sql);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(object);
            }
        }
    }
}
