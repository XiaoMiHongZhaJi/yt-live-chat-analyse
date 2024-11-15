package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwf.ytlivechatanalyse.bean.*;
import com.lwf.ytlivechatanalyse.dao.HotListMapper;
import com.lwf.ytlivechatanalyse.dao.LiveChatDataMapper;
import com.lwf.ytlivechatanalyse.dao.LivingChatDataMapper;
import com.lwf.ytlivechatanalyse.interceptor.DynamicSchemaInterceptor;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.DateUtil;
import com.lwf.ytlivechatanalyse.util.MessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyseService {

    private final Logger logger = LoggerFactory.getLogger(AnalyseService.class);

    @Autowired
    LiveChatDataService liveChatDataService;

    @Autowired
    LiveInfoService liveInfoService;

    @Autowired
    HotListMapper hotListMapper;

    @Autowired
    LiveChatDataMapper liveChatDataMapper;

    @Autowired
    LivingChatDataMapper livingChatDataMapper;

    @Autowired
    SqlSessionFactory sqlSessionFactory;


    public List<HotList> queryHotList(LiveInfo liveInfo, Integer intervalMinutes, String keyword){
        if(intervalMinutes == null || intervalMinutes < 1){
            intervalMinutes = 1;
        }
        //开播日期
        String liveDate = liveInfo.getLiveDate();
        if(!liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        //间隔秒数
        int intervalSeconds = intervalMinutes * 60;
        List<HotList> hotListList = new ArrayList<>();
        if(StringUtils.isBlank(keyword)){
            QueryWrapper<HotList> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("live_date", liveDate);
            queryWrapper.eq("interval_seconds", intervalSeconds);
            queryWrapper.orderByAsc("id");
            // 查询缓存
            hotListList = hotListMapper.selectList(queryWrapper);
            if(!CollectionUtils.isEmpty(hotListList)){
                return hotListList;
            }
        }
        liveInfo = liveInfoService.selectOne(liveInfo);
        if(liveInfo == null){
            return hotListList;
        }
        //直播状态
        String liveStatus = liveInfo.getLiveStatus();
        //开始时间戳
        Long startTimestamp = liveInfo.getStartTimestamp();
        LiveChatData liveChatData = new LiveChatData();
        liveChatData.setLiveDate(liveDate);
        liveChatData.setMessage(keyword);
        List<LiveChatData> liveChatAll = liveChatDataService.selectList(liveChatData, true);
        if(CollectionUtils.isEmpty(liveChatAll)){
            liveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_NONE);
            List<LivingChatData> livingChatData = liveChatDataService.selectLivingList(liveChatData, true);
            if(CollectionUtils.isEmpty(livingChatData)){
                return hotListList;
            }
            liveChatAll.addAll(livingChatData);
        }
        //开始秒数，第一条弹幕的发送时间
        int startSecond = 0;
        //累计条数
        int totalCount = 0;
        if(startTimestamp == null) {
            startTimestamp = liveChatAll.get(0).getTimestamp();
        }
        //组装数据
        List<LiveChatData> liveChatList = new ArrayList<>();
        for (LiveChatData liveChat : liveChatAll){
            long timestamp = liveChat.getTimestamp() - startTimestamp;
            if(timestamp < 0){
                continue;
            }
            BigDecimal timeInSeconds = new BigDecimal(timestamp / 1000000);
            //弹幕发送时间
            double sendtime = Double.parseDouble(timeInSeconds.toString());
            while (sendtime >  startSecond + intervalSeconds){
                //超出时间段
                HotList hotList = getHotList(startSecond, intervalSeconds, liveChatList);
                totalCount += liveChatList.size();
                hotList.setTotalCount(totalCount);
                if(liveChatList.size() > 0){
                    hotList.setStartTimestamp(liveChatList.get(0).getTimestamp());
                }
                hotList.setIntervalSeconds(intervalSeconds);
                hotList.setLiveDate(liveDate);
                hotListList.add(hotList);
                liveChatList.clear();
                startSecond += intervalSeconds;
            }
            liveChatList.add(liveChat);
        }
        if(StringUtils.isBlank(keyword) &&
                LiveInfo.LIVE_STATUS_DONE.equals(liveStatus) &&
                LiveInfo.DOWNLOAD_STATUS_DONE.equals(liveInfo.getDownloadStatus())){
            batchInsertHotList(hotListList);
        }
        return hotListList;
    }

    private void batchInsertHotList(List<HotList> hotListList) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            for (HotList hotList : hotListList) {
                sqlSession.insert("com.lwf.ytlivechatanalyse.dao.HotListMapper.insert", hotList);
            }
            sqlSession.commit();
        } catch (Exception e) {
            logger.error("批量插入出错", e);
            for (HotList hotList : hotListList) {
                try {
                    String liveDate = hotList.getLiveDate();
                    if(!liveDate.startsWith(Constant.DEFAULT_YEAR)){
                        DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
                    }
                    hotListMapper.insert(hotList);
                } catch (Exception e1) {
                    logger.error("批量插入出错，已改为单个插入，错误数据：", e1);
                    logger.error(hotList.toString());
                }
            }
        }
    }

    private HotList getHotList(int startSecond, int intervalSeconds, List<LiveChatData> liveChatList){
        HotList hotList = new HotList();
        hotList.setStartSecond(startSecond);
        hotList.setStartTime(DateUtil.secondToString(startSecond));
        hotList.setEndTime(DateUtil.secondToString(startSecond + intervalSeconds));
        hotList.setCount(liveChatList.size());
        List<Object> messages = new ArrayList<>();
        Map<String,LiveChatData> messagesMap = new HashMap<>();
        Map<String,Integer> specWordMap = new HashMap<>();
        for (Map.Entry<String,String> entry : Constant.ANALYSE_MERGE_WORD.entrySet()){
            String value = entry.getValue();
            specWordMap.put(value,0);
        }
        //去重复
        for (LiveChatData liveChatData : liveChatList){
            String message = liveChatData.getMessage();
            if (StringUtils.isBlank(message)){
                continue;
            }
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
        messages.sort((Object o1, Object o2) -> Integer.parseInt(((Object[])o2)[1].toString()) - Integer.parseInt(((Object[])o1)[1].toString()));
        if(messages.size() > Constant.ANALYSE_CHAT_COUNT){
            messages = messages.subList(0,Constant.ANALYSE_CHAT_COUNT);
        }
        hotList.setMessages(messages);
        return hotList;
    }

    public List queryHotListDetail(String liveDate, Long startTimestamp, Integer intervalMinutes){
        Long endTimestamp = startTimestamp + intervalMinutes * 60 * 1000000;
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("live_date", liveDate);
        queryWrapper.ge("timestamp", startTimestamp);
        queryWrapper.lt("timestamp", endTimestamp);
        queryWrapper.orderByAsc("timestamp");
        if(!liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        List liveChatAll = liveChatDataMapper.selectList(queryWrapper);
        LiveInfo queryInfo = new LiveInfo();
        queryInfo.setLiveDate(liveDate);
        if(CollectionUtils.isEmpty(liveChatAll)){
            //直播中
            QueryWrapper<LivingChatData> queryWrapperLiving = new QueryWrapper<>();
            queryWrapperLiving.likeRight("live_date", liveDate);
            queryWrapperLiving.ge("timestamp", startTimestamp);
            queryWrapperLiving.lt("timestamp", endTimestamp);
            queryWrapperLiving.orderByAsc("timestamp");
            liveChatAll = livingChatDataMapper.selectList(queryWrapperLiving);
        }
        return liveChatAll;
    }

}
