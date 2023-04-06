package com.lwf.ytlivechatanalyse.util;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BulletUtil {

    private static final Logger logger = LoggerFactory.getLogger(BulletUtil.class);

    private static final String TYPE_SCROLL = "1"; //滚动弹幕
    private static final String TYPE_BOTTOM = "4"; //底部弹幕
    private static final String TYPE_TOP = "5"; //顶部弹幕

    private static final String FONT_SIZE = "15"; //字体大小

    private static final String FONT_COLOR_WHITE = "16777215"; //字体颜色

    private static final String FONT_COLOR_RED = "16711680"; //字体颜色

    private static final String FONT_COLOR_GRAY = "8421504";

    private static final Map<String, String> specType = getSpecType();

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
        map.put("旅行_", TYPE_TOP);
        map.put("赵迪奥", TYPE_TOP);
        map.put("台主真是太强乐", TYPE_TOP);
        map.put("robin罗宾", TYPE_TOP);
        map.put("旅行_带着王总去旅行", TYPE_TOP);
        map.put("放映室检票员", TYPE_TOP);
        map.put("村西透", TYPE_TOP);
        map.put("带着小德去旅行", TYPE_TOP);
        map.put("呱呱", TYPE_BOTTOM);
        map.put("呱呱盒子", TYPE_BOTTOM);
        map.put("拥抱", TYPE_BOTTOM);
        map.put("萝菽菽", TYPE_BOTTOM);
        map.put("seaLLee", TYPE_BOTTOM);
        return map;
    }

    private static String bulletConver;

    @Value("${bulletConver}")
    public void setBulletConver(String bulletConver) {
        this.bulletConver = bulletConver;
    }

    public static void main(String[] args) {
        File xmlFile = getBulletXml("2023-03-27", "E:\\67373\\直播录像\\");
    }

    private static File getBulletXml(String liveDate, String filePath){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        long startTimestamp = 0;
        List<LiveChatData> chatList = new ArrayList<>();
        String queryStartTimestamp = "select start_timestamp from live_info where live_date = ? and start_timestamp is not null";
        String sql = "select author_name, message, timestamp from live_chat_data where live_date like concat(?,'%') ";
        sql += "order by timestamp";
        try {
            conn = JDBCUtil.getConnection();
            ps = conn.prepareStatement(queryStartTimestamp);
            ps.setString(1, liveDate);
            rs = ps.executeQuery();
            if(rs.next()){
                startTimestamp = rs.getLong(1);
            }
            JDBCUtil.closeStatement(ps, rs);
            ps = conn.prepareStatement(sql);
            ps.setString(1, liveDate);
            ps.setString(2, liveDate);
            rs = ps.executeQuery();
            while(rs.next()){
                LiveChatData liveChatData = new LiveChatData();
                liveChatData.setAuthorName(rs.getString("author_name"));
                liveChatData.setMessage(rs.getString("message"));
                liveChatData.setTimestamp(rs.getLong("timestamp"));
                chatList.add(liveChatData);
            }
            JDBCUtil.closeStatement(ps, rs);
        }catch (Exception e){
            logger.error("sql执行失败", e);
        }finally {
            JDBCUtil.closeConnection(conn, ps, rs);
        }
        //生成xml
        if(!filePath.endsWith("\\") && filePath.contains("\\")){
            filePath = filePath + "\\";
        }
        if(!filePath.endsWith("/") && filePath.contains("/")){
            filePath = filePath + "/";
        }
        File file = new File(filePath + liveDate + ".xml");
        if(file.exists()){
            file.delete();
        }
        BufferedWriter writer = null;
        try{
            writer = new BufferedWriter(new FileWriter(file));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.newLine();
            writer.write("<i>");
            writer.newLine();
            //<d p="1568.69900,1,25,16777215,1527417135,0,96de7b6f,144158263607298">熊半城</d>
            for(LiveChatData liveChatData : chatList){
                String line = getBulletLine(startTimestamp, liveChatData);
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
        }
        return file;
    }

    private static String getBulletLine(Long startTimestamp, LiveChatData liveChatData) {
        Long timestamp = liveChatData.getTimestamp();
        long time = timestamp - startTimestamp;
        double second = (double) time / 1000000;
        if(second < -30){
            return null;
        }
        if(second < 0){
            second = 0;
        }
        String authorName = liveChatData.getAuthorName();
        String message = liveChatData.getMessage();
        message = message.replaceAll("[&<>\u0000-\u0019]","_");
        String type = TYPE_SCROLL;
        String fontColor = FONT_COLOR_WHITE;
        StringBuffer line = new StringBuffer("<d p=\"");
        line.append(second);
        line.append(",");
        String spec = specType.get(authorName);
        if(spec != null){
            type = spec;
            fontColor = FONT_COLOR_RED;
        }
        line.append(type);
        line.append(",");
        line.append(FONT_SIZE);
        line.append(",");
        line.append(fontColor);
        line.append(",0\">");
        line.append(authorName);
        line.append("：");
        line.append(message);
        line.append("</d>");
        return line.toString();
    }

    public static File getXmlFile(List<LiveChatData> chatList, Long startTimestamp) {
        if(CollectionUtils.isEmpty(chatList)){
            return null;
        }
        Long minTimestamp = chatList.get(0).getTimestamp();
        Long maxTimestamp = chatList.get(chatList.size() - 1).getTimestamp();
        if(startTimestamp == null || startTimestamp < minTimestamp){
            startTimestamp = minTimestamp;
        }else if(startTimestamp > maxTimestamp){
            startTimestamp = maxTimestamp;
        }
        BufferedWriter writer = null;
        File file = new File("bullet\\" + startTimestamp + ".xml");
        try{
            writer = new BufferedWriter(new FileWriter(file));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.newLine();
            writer.write("<i>");
            writer.newLine();
            for(LiveChatData liveChatData : chatList){
                String line = getBulletLine(startTimestamp, liveChatData);
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
                writer.close();
            }catch(IOException e){
                logger.error("关闭流失败", e);
            }
        }
        return file;
    }

    public static File converBulletAss(File file) {
        if(StringUtils.isNotBlank(bulletConver) && file != null){
            CmdUtil.execCmd("\"" + bulletConver + "\" " + file.getAbsolutePath());
            File fileAss = new File(file.getAbsolutePath().replace(".xml", ".ass"));
            if(fileAss.exists()){
                return fileAss;
            }
        }
        return null;
    }
}
