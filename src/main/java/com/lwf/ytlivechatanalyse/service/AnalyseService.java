package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwf.ytlivechatanalyse.bean.HotList;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.dao.HotListMapper;
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
    HotListMapper hotListMapper;

    @Autowired
    SqlSessionFactory sqlSessionFactory;


    public List<HotList> queryHotList(LiveChatData liveChatData, Integer intervalMinutes, String liveStatus){
        if(intervalMinutes == null || intervalMinutes < 1){
            intervalMinutes = 1;
        }
        //间隔秒数
        int intervalSeconds = intervalMinutes * 60;
        QueryWrapper<HotList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveChatData.getLiveDate());
        queryWrapper.eq("interval_seconds", intervalSeconds);
        queryWrapper.orderByAsc("id");
        List<HotList> hotListList = hotListMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(hotListList)){
            return hotListList;
        }
        List<LiveChatData> liveChatAll = null;
        if(LiveInfo.LIVE_STATUS_DONE.equals(liveStatus)){
            //直播结束
            liveChatAll = liveChatDataService.selectList(liveChatData, true);
        }
        if(CollectionUtils.isEmpty(liveChatAll)){
            liveStatus = LiveInfo.LIVE_STATUS_LIVEING;
            liveChatAll = liveChatDataService.selectLivingList(liveChatData, true);
            if(CollectionUtils.isEmpty(liveChatAll)){
                return hotListList;
            }
        }
        //开始秒数，第一条弹幕的发送时间
        int startSecond = 0;
        //累计条数
        int totalCount = 0;
        //开播日期
        String liveDate = liveChatData.getLiveDate();
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
                totalCount += liveChatList.size();
                hotList.setTotalCount(totalCount);
                hotList.setIntervalSeconds(intervalSeconds);
                hotList.setLiveDate(liveDate);
                hotListList.add(hotList);
                liveChatList.clear();
                startSecond += intervalSeconds;
            }
            liveChatList.add(liveChat);
            /*if(i == liveChatAll.size() - 1){
                //最后一个区间
                HotList hotList = getHotList(startSecond, intervalSeconds, liveChatList);
                totalCount += liveChatList.size();
                hotList.setTotalCount(totalCount);
                hotList.setIntervalSeconds(intervalSeconds);
                hotList.setLiveDate(liveDate);
                hotListList.add(hotList);
                liveChatList.clear();
            }*/
        }
        if(LiveInfo.LIVE_STATUS_DONE.equals(liveStatus) && !CollectionUtils.isEmpty(hotListList)){
            batchInsertHotList(hotListList);
        }
        return hotListList;
    }

    private void batchInsertHotList(List<HotList> hotListList) {
        SqlSession sqlSession = null;
        try{
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            for (HotList hotList : hotListList){
                sqlSession.insert("com.lwf.ytlivechatanalyse.dao.HotListMapper.insert", hotList);
            }
            sqlSession.flushStatements();
        }catch (Exception e){
            logger.error(e.getMessage());
            for (HotList hotList : hotListList){
                try {
                    hotListMapper.insert(hotList);
                }catch (Exception e1){
                    logger.error("批量插入出错，已改为单个插入，错误数据：");
                    logger.error(hotList.toString());
                    logger.error(e1.getMessage());
                }
            }
        }finally {
            if(sqlSession != null)
                sqlSession.close();
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
        messages.sort((Object o1, Object o2) -> Integer.parseInt(((Object[])o2)[1].toString()) - Integer.parseInt(((Object[])o1)[1].toString()));
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
        List<LiveChatData> liveChatAll = hotListMapper.selectLiveHotListDeail(liveDate, startSecond, endSecond);;
        LiveInfo queryInfo = new LiveInfo();
        queryInfo.setLiveDate(liveDate);
        if(CollectionUtils.isEmpty(liveChatAll)){
            //直播中
            liveChatAll = hotListMapper.selectLivingHotListDeail(liveDate, startSecond, endSecond);
        }
        return liveChatAll;
    }

}
