package com.lwf.ytlivechatanalyse.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwf.ytlivechatanalyse.bean.*;
import com.lwf.ytlivechatanalyse.dao.LiveChatDataMapper;
import com.lwf.ytlivechatanalyse.dao.LivingChatDataMapper;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.DateUtil;
import com.lwf.ytlivechatanalyse.util.JsonUtil;
import com.lwf.ytlivechatanalyse.util.MessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class LiveChatDataService {

    private final Logger logger = LoggerFactory.getLogger(LiveChatDataService.class);

    @Autowired
    LiveChatDataMapper liveChatDataMapper;

    @Autowired
    LivingChatDataMapper livingChatDataMapper;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    EmotesDataService emotesDataService;

    @Autowired
    LiveInfoService liveInfoService;

    public List<LiveChatData> selectList(LiveChatData liveChatData, boolean isAsc){
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(liveChatData.getKeyWords())){
            queryWrapper.like("message", liveChatData.getKeyWords());
        }
        if(StringUtils.isNotBlank(liveChatData.getAuthorName())){
            queryWrapper.like("author_name", liveChatData.getAuthorName());
        }
        if(StringUtils.isNotBlank(liveChatData.getLiveDate())){
            queryWrapper.like("live_date", liveChatData.getLiveDate());
        }
        queryWrapper.orderBy(true, isAsc, "timestamp");
        return liveChatDataMapper.selectList(queryWrapper);
    }

    public List<LiveChatData> selectLivingList(LiveChatData liveChatData, boolean isAsc){
        return livingChatDataMapper.selectLivingList(liveChatData, isAsc);
    }

    public Long selectStartTimestamp(String liveDate){
        Long selectStartTimestamp = liveChatDataMapper.selectStartTimestamp(liveDate);
        if(selectStartTimestamp == null || selectStartTimestamp == 0){
            selectStartTimestamp = liveChatDataMapper.selectStartTimestampByMessage(liveDate);
        }
        return selectStartTimestamp;
    }

    public Integer updateTimestamp(String liveDate,Long timestamp){
        return liveChatDataMapper.updateTimestamp(liveDate, timestamp);
    }

    public Long selectCount(LiveInfo liveInfo){
        if(LiveInfo.LIVE_STATUS_DONE.equals(liveInfo.getLiveStatus())){
            QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("live_date", liveInfo.getLiveDate());
            return liveChatDataMapper.selectCount(queryWrapper);
        }
        //直播中，直播预告
        QueryWrapper<LivingChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveInfo.getLiveDate());
        return livingChatDataMapper.selectCount(queryWrapper);
    }

    public void insertBatch(List<LiveChatData> batchList){
        SqlSession sqlSession = null;
        try{
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            for (LiveChatData liveChatData : batchList){
                sqlSession.insert("com.lwf.ytlivechatanalyse.dao.LiveChatDataMapper.insertNotExists", liveChatData);
            }
            sqlSession.flushStatements();
        }catch (Exception e){
            for (LiveChatData liveChatData : batchList){
                try {
                    liveChatDataMapper.insertNotExists(liveChatData);
                }catch (Exception e1){
                    logger.error("批量插入出错，已改为单笔插入，错误数据：");
                    logger.error(liveChatData.toString());
                }
            }
        }finally {
            if(sqlSession != null)
                sqlSession.close();
        }
    }

    public List<HotList> queryHotList(LiveChatData liveChatData, Integer intervalMinutes, String liveStatus){
        List<LiveChatData> liveChatAll;
        if("1".equals(liveStatus)){
            //直播中
            liveChatAll = selectLivingList(liveChatData, true);
        }else {
            liveChatAll = selectList(liveChatData, true);
        }
        List<HotList> hotListList = new ArrayList<>();
        if (liveChatAll.size() == 0){
            return hotListList;
        }
        //开始秒数，第一条弹幕的发送时间
        int startSecond = 0;
        //间隔秒数
        int intervalSeconds = intervalMinutes == null || intervalMinutes < 1 ? 60 : intervalMinutes * 60;
        //组装数据
        List<LiveChatData> liveChatList = new ArrayList<>();
        for (int i = 0; i < liveChatAll.size(); i++){
            LiveChatData liveChat = liveChatAll.get(i);
            BigDecimal timeInSeconds = liveChat.getTimeInSeconds();
            if(timeInSeconds == null || timeInSeconds.compareTo(new BigDecimal(0)) < 0){
                continue;
            }
            //弹幕发送时间
            double sendtime = Double.parseDouble(timeInSeconds.toString());
            if (sendtime >  startSecond + intervalSeconds){
                //超出时间段
                HotList hotList = getHotList(startSecond, intervalSeconds, liveChatList);
                hotListList.add(hotList);
                liveChatList.clear();
                startSecond += intervalSeconds;
            }
            liveChatList.add(liveChat);
            if(i == liveChatAll.size() - 1){
                //最后一个区间
                HotList hotList = getHotList(startSecond, intervalSeconds, liveChatList);
                hotListList.add(hotList);
                liveChatList.clear();
            }
        }

        return hotListList;
    }

    public String getJsonString(LiveChatData liveChatData){
        List<LiveChatData> liveChatAll = selectList(liveChatData, true);
        List<Map<String,Object>> tempList = new ArrayList<>();
        for (int i = 0; i < liveChatAll.size(); i++){
            Map<String,Object> temp = new HashMap<>();
            LiveChatData liveChat = liveChatAll.get(i);
            temp.put("authorImage",liveChat.getAuthorImage());
            temp.put("authorName",liveChat.getAuthorName());
            temp.put("emotesCount",liveChat.getEmotesCount());
            temp.put("message",liveChat.getMessage());
            temp.put("timeText",liveChat.getTimeText());
            temp.put("timeInSeconds",liveChat.getTimeInSeconds());
            tempList.add(temp);
        }
        String jsonString = JSON.toJSONString(tempList);
        return jsonString;
    }

    private HotList getHotList(int startSecond, int intervalSeconds, List<LiveChatData> liveChatList){
        HotList hotList = new HotList();
        hotList.setStartSecond(startSecond);
        hotList.setStartTime(DateUtil.secondToString(startSecond));
        hotList.setEndTime(DateUtil.secondToString(startSecond + intervalSeconds));
        hotList.setCount(liveChatList.size());
        List<Object[]> messages = new ArrayList<>();
        Map<String,LiveChatData> messagesMap = new HashMap<>();
        Map<String,Integer> specWordMap = new HashMap<>();
        for (Map.Entry<String,String> entry : Constant.ANALYSE_MERGE_WORD.entrySet()){
            String value = entry.getValue();
            specWordMap.put(value,0);
        }
        //去重复
        for (LiveChatData liveChatData : liveChatList){
            String message = liveChatData.getMessage();
            boolean isSpecWord = false;
            for (Map.Entry<String,String> entry : Constant.ANALYSE_MERGE_WORD.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();
                if (MessageUtil.getProportion(message,key.charAt(0)) >= Constant.ANALYSE_MERGE_WORD_PROPORTION){
                    specWordMap.put(value,specWordMap.get(value) + 1);
                    isSpecWord = true;
                    break;
                }
            }
            if (isSpecWord){
                continue;
            }
            Integer emotesCount = liveChatData.getEmotesCount();
            if(emotesCount == null || emotesCount == 0){
                message = message.replaceAll(" |啊|阿|吧|\\pP|\\pS","");
            }
            if (StringUtils.isBlank(message)){
                continue;
            }
            LiveChatData data = messagesMap.get(message);
            if(data == null){
                liveChatData.setCount(1);
                messagesMap.put(message,liveChatData);
            }else {
                Integer count = data.getCount();
                data.setCount(count + 1);
            }
        }
        //整理
        for (Map.Entry<String,LiveChatData> entry : messagesMap.entrySet()){
            String message = entry.getKey();
            if(message == null || message.length() < Constant.ANALYSE_MERGE_LENGTH){
                continue;
            }
            LiveChatData data = entry.getValue();
            Integer count = data.getCount();
            if(count == null){
                continue;
            }
            int flag = 0;
            Integer flagValue = null;
            for (Map.Entry<String,LiveChatData> temp : messagesMap.entrySet()){
                String key = temp.getKey();
                if(key == null || key.length() < Constant.ANALYSE_MERGE_LENGTH){
                    continue;
                }
                key = key.replace(" ","");
                LiveChatData dataTemp = temp.getValue();
                Integer value = dataTemp.getCount();
                if(value == null || key.equals(message)){
                    //去掉自己 和 已合并的key
                    continue;
                }
                if(message.contains(key) && key.length() * 100 / message.length() > Constant.ANALYSE_MERGE_WORD_PROPORTION){
                    flag = 1;
                    flagValue = value;
                    dataTemp.setCount(null);
                    break;
                }else if(key.contains(message) && message.length() * 100 / key.length() > Constant.ANALYSE_MERGE_WORD_PROPORTION){
                    flag = 2;
                    flagValue = value;
                    dataTemp.setCount(count + flagValue);
                    break;
                }
            }
            if(flag == 1){
                data.setCount(count + flagValue);
            }else if(flag == 2){
                data.setCount(null);
            }
        }
        for (Map.Entry<String,LiveChatData> entry : messagesMap.entrySet()){
            String message = entry.getKey();
            LiveChatData data = entry.getValue();
            Integer count = data.getCount();
            Integer emotesCount = data.getEmotesCount();
            if(count != null && message != null){
                if(message.length() > Constant.ANALYSE_MESSAGE_LENGTH && !message.contains(":") && !message.contains(" ")){
                    message = message.substring(0,Constant.ANALYSE_MESSAGE_LENGTH) + "...";
                }
                messages.add(new Object[]{message,count,emotesCount});
            }
        }
        for (Map.Entry<String,Integer> entry : specWordMap.entrySet()){
            String message = entry.getKey();
            Integer count = entry.getValue();
            if(count > 0){
                messages.add(new Object[]{message,count,0});
            }
        }
        messages.sort((Object[] o1, Object[] o2) -> {
            return Integer.parseInt(o2[1].toString()) - Integer.parseInt(o1[1].toString());
        });
        if(messages.size() > Constant.ANALYSE_CHAT_COUNT){
            messages = messages.subList(0,Constant.ANALYSE_CHAT_COUNT);
        }
        hotList.setMessages(messages);
        return hotList;
    }

    public List<LiveChatData> queryHotListDetail(String liveDate, String startTime, Integer intervalMinutes){
        String[] split = startTime.split(":");
        int startSecond = 0;
        if(split.length == 3){
            startSecond = Integer.parseInt(split[0]) * 3600 + Integer.parseInt(split[1]) * 60 + Integer.parseInt(split[2]);
        }else if(split.length == 2){
            startSecond = Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1]);
        }else{
            startSecond = Integer.parseInt(split[0]);
        }
        int endSecond = startSecond + intervalMinutes * 60;
        List<LiveChatData> liveChatAll = null;
        LiveInfo queryInfo = new LiveInfo();
        queryInfo.setLiveDate(liveDate);
        LiveInfo liveInfo = liveInfoService.selectOne(queryInfo);
        if("1".equals(liveInfo.getLiveStatus())){
            //直播中
            liveChatAll = livingChatDataMapper.selectHotListDeail(liveDate, startSecond, endSecond);
        }else {
            liveChatAll = liveChatDataMapper.selectHotListDeail(liveDate, startSecond, endSecond);
        }
        return liveChatAll;
    }

    public void deleteByLiveDate(String liveDate){
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveDate);
        liveChatDataMapper.delete(queryWrapper);
    }
}
