package com.lwf.ytlivechatanalyse.util;

import com.lwf.ytlivechatanalyse.bean.BulletConfig;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.service.EmotesDataService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Component
public class BulletAssUtil {

    private static final Logger logger = LoggerFactory.getLogger(BulletAssUtil.class);

    private static final String TYPE_SCROLL = "Scroll"; //滚动弹幕
    private static final String TYPE_BOTTOM = "Bottom"; //底部弹幕
    private static final String TYPE_TOP = "Top"; //顶部弹幕
    private static final int SCREEN_WIDTH = 1280; //屏幕宽度
    private static final int SCREEN_HEIGHT = 720; //屏幕高度
    //B G R
    private static final String FONT_COLOR_WHITE = "ffffff"; //白色
    private static final String FONT_COLOR_BLUE = "f1845e"; //蓝色
    private static final String FONT_COLOR_RED = "0000c7"; //红色

    private static final Map<String, String> specType = getSpecType();

    private static Map<String, String> emotesMap;

    private static EmotesDataService emotesDataService;

    @Autowired
    public void setEmotesDataService(EmotesDataService emotesDataService) {
        BulletAssUtil.emotesDataService = emotesDataService;
    }

    private static void initEmoteMap() {
        List<EmotesData> emotesData = emotesDataService.selectEmoji();
        emotesMap = new HashMap<>();
        for(EmotesData emote : emotesData){
            emotesMap.put(emote.getName(), emote.getEmotesId());
        }
    }

    public static void main(String[] args) throws Exception{

        String liveDate = "2023-10-09";
        Long startTimestamp = getStartTimestamp(liveDate);
        startTimestamp = DateUtil.getTimestamp("2023-10-09_21-55-11");
        List<LiveChatData> chatList = getChatList(liveDate);
        if(chatList.size() == 0){
            return;
        }
        initEmoteMapLocal();
        BulletConfig config = new BulletConfig();
//        config.setOffset(5558);
        getAssFile(chatList, startTimestamp, config);
    }

    /**
     * 生成ass弹幕文件
     */
    public static Result getAssFile(List<LiveChatData> chatList, Long startTimestamp, BulletConfig config) {
        int offset = config.getOffset();
        int duringSecond = config.getDuringSecond();
        int fontSize = config.getFontSize();
        int lineSpace = config.getLineSpace();
        int bulletBlock = config.getBulletBlock();
        boolean blocked = config.isBlocked();
        int speed = SCREEN_WIDTH * 4 / duringSecond;

        Long minTimestamp = chatList.get(0).getTimestamp();
        Long maxTimestamp = chatList.get(chatList.size() - 1).getTimestamp();
        if(startTimestamp == null || startTimestamp < minTimestamp){
            startTimestamp = minTimestamp;
        }else if(startTimestamp > maxTimestamp){
            startTimestamp = maxTimestamp;
        }
        String fileName = DateUtil.getDateTime(startTimestamp) + ".ass";
        if(offset != 0){
            fileName = DateUtil.getDateTime(startTimestamp) + "_" + offset + ".ass";
            startTimestamp += offset * 1000000L;
        }
        if(duringSecond <= 5){
            duringSecond = 5;
        }
        StringBuffer assContent = new StringBuffer();
        String filePath = "bullet/ass";
        File floder = new File(filePath);
        if(!floder.exists()){
            floder.mkdirs();
        }
        filePath = floder.getAbsolutePath();
        if(filePath.contains("\\")){
            filePath = filePath + "\\";
        }
        if(filePath.contains("/")){
            filePath = filePath + "/";
        }
        StringBuffer log = new StringBuffer(512);
        int currentHeight = 0;
        int cycleIndex = 0;
        int loseCount = 0;
        double lastCycleSecond = 0;
        byte[] array = new byte[2048];
        //块区内丢弃条数
        int blockLoseCount = 0;
        //临时减小字体大小
        int downFontSize = 0;
        try{
            int read = new ClassPathResource("static/assets/head.ass").getInputStream().read(array);
            String head = new String(array, 0, read);
            head = head.replace("{title}", fileName);
            head = head.replace("{font_size}", String.valueOf(fontSize));
            assContent.append(head);
            for(LiveChatData liveChatData : chatList){
                double second = (double) (liveChatData.getTimestamp() - startTimestamp) / 1000000;
                int blockSecond = blocked ? duringSecond / bulletBlock : 0;
                if(second < -60){
                    continue;
                }
                if(second < 0){
                    second = 0;
                }else if(blockSecond > 0){
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
                    if(BulletAssUtil.TYPE_BOTTOM.equals(type)){
                        //房管
                        fontColor = FONT_COLOR_BLUE;
                    }else if(BulletAssUtil.TYPE_TOP.equals(type)){
                        //特殊
                        fontColor = FONT_COLOR_RED;
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
                            char[] charArray = scInfo.substring(index + 1, index + 7).toCharArray();
                            fontColor = String.valueOf(new char[]{charArray[4], charArray[5], charArray[2], charArray[3], charArray[0], charArray[1]});
                        }
                    }
                }
                if(StringUtils.isBlank(authorName)){
                    continue;
                }
                message = authorName + message;

                int bulletCycleSecond = blockSecond > 0 ? blockSecond : (duringSecond / bulletBlock);
                if(second - lastCycleSecond >= bulletCycleSecond){
                    //区块结束
                    lastCycleSecond = second;
                    currentHeight = 0;
                    cycleIndex ++;
                    //当弹幕密度过大时降低下一区块字体大小
                    if(blockLoseCount > 0 && downFontSize < 10){
                        downFontSize += 5;
                        blockLoseCount = 0;
                    //当弹幕密度降低时恢复下一区块字体大小
                    }else if(blockLoseCount == 0 && downFontSize > 0){
                        downFontSize -= 5;
                    }
                }

                if(currentHeight > SCREEN_HEIGHT){
                    log.append(String.format("\n%s 弹幕密度过高，已丢弃：%s", DateUtil.secondToString(second), message));
                    loseCount ++;
                    blockLoseCount ++;
                    continue;
                }

                int lineHeight = currentHeight + (cycleIndex % 2 == 1 ? 15 : 0);

                //Dialogue: 2,0:00:10.00,0:00:22.00,Float,,0,0,0,,{\move(1280, 25, -240, 25)\c&Hffffff&}为辣个女人：来了
                String line = String.format("Dialogue: 2,%s,%s,%s,,0,0,0,,{\\move(%d, %d, -%d, %d)\\c&H%s&}%s",
                        DateUtil.secondToString(second), DateUtil.secondToString(second + duringSecond), type, SCREEN_WIDTH, lineHeight, speed, lineHeight, fontColor, message);

                currentHeight += fontSize + lineSpace;

                assContent.append(line);
                assContent.append("\n");
            }
            if(loseCount > 0){
                log.insert(0, String.format("弹幕总条数：%s 丢弃条数：%s", chatList.size(), loseCount));
            }
            assContent.append("\n");
        }catch(Exception e){
            logger.error("生成ass文件失败", e);
            return new Result(500, "生成ass文件失败：" + e.getMessage());
        }
        logger.info(log.toString());
        List<String> result = new ArrayList();
        result.add(assContent.toString());
        result.add(fileName);
        result.add(log.toString());
        return new Result(200, "生成ass文件成功", result);
    }

    private static String getEmoteMssage(String message) {
        int index = message.indexOf(":");
        if(index == -1){
            return message;
        }
        //YouTube
        StringBuilder emoteMssage = new StringBuilder(message.substring(0, index));
        String remain = message.substring(index);
        int endIndex = remain.indexOf(":", 1);
        String lastEmote = null;
        while(endIndex > 2){
            String key = remain.substring(1, endIndex);
            if(emotesMap == null){
                initEmoteMap();
            }
            String emote = emotesMap.get(key);
            if(StringUtils.isBlank(emote)){
                if(!key.equals(lastEmote)){
                    emoteMssage.append(String.format("[%s]", key));
                    lastEmote = key;
                }
            }else{
                emoteMssage.append(emote);
            }
            remain = remain.substring(endIndex + 1);
            int startIndex = remain.indexOf(":");
            if(startIndex > 0){
                emoteMssage.append(remain, 0, startIndex);
                remain = remain.substring(startIndex);
            }
            endIndex = remain.indexOf(":", 1);
        }
        emoteMssage.append(remain);
        return emoteMssage.toString();
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
            if(chatList.size() == 0){
                JDBCUtil.closeStatement(ps, rs);
                ps = conn.prepareStatement(sql.toString().replace("live_chat_data", "living_chat_data"));
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
        map.put("AMX002S", TYPE_TOP);
        map.put("amx002s", TYPE_TOP);
        map.put("WS0124", TYPE_TOP);
        map.put("Circle莫年", TYPE_TOP);
        map.put("孤独时代的微醺", TYPE_TOP);
        map.put("1秒1卡", TYPE_TOP);
        map.put("孤獨時代的一分一秒", TYPE_TOP);
        map.put("我发超可爱呢", TYPE_TOP);
        map.put("瀧角散", TYPE_TOP);
        map.put("李友辰", TYPE_TOP);
        map.put("TouHaoFaChui 头号发吹", TYPE_TOP);
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
        map.put("萝菽菽_", TYPE_BOTTOM);
        map.put("seaLLee", TYPE_BOTTOM);
        map.put("陈一发儿", TYPE_BOTTOM);
        return map;
    }
}
