package com.lwf.ytlivechatanalyse.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.bean.LivingChatData;
import com.lwf.ytlivechatanalyse.dao.LiveChatDataMapper;
import com.lwf.ytlivechatanalyse.dao.LivingChatDataMapper;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.DateUtil;
import com.lwf.ytlivechatanalyse.util.JsonUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<LiveChatData> selectList(LiveChatData liveChatData, boolean isAsc){
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(liveChatData.getMessage())){
            queryWrapper.like("message", liveChatData.getMessage());
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

    public int selectCount(String liveDate){
        //录像
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveDate);
        return Math.toIntExact(liveChatDataMapper.selectCount(queryWrapper));
    }

    public int selectLivingCount(String liveDate){
        //直播中，直播预告
        QueryWrapper<LivingChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveDate);
        return Math.toIntExact(livingChatDataMapper.selectCount(queryWrapper));
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

    public void deleteByLiveDate(String liveDate){
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveDate);
        liveChatDataMapper.delete(queryWrapper);
    }

    public void addLivingChatList(List<LivingChatData> livingChatList) {
        if(livingChatList.size() == 1){
            LivingChatData livingChatData = livingChatList.get(0);
            livingChatData.setLiveDate(DateUtil.getNowDate());
            logger.info(livingChatData.showDetail());
            livingChatDataMapper.insert(livingChatData);
            return;
        }
        SqlSession sqlSession = null;
        try{
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            for (LivingChatData livingChatData : livingChatList){
                livingChatData.setLiveDate(DateUtil.getNowDate());
                logger.info(livingChatData.showDetail());
                sqlSession.insert("com.lwf.ytlivechatanalyse.dao.LivingChatDataMapper.insert", livingChatData);
            }
            sqlSession.flushStatements();
        }catch (Exception e){
            logger.error(e.getMessage());
            for (LivingChatData livingChatData : livingChatList) {
                try {
                    livingChatDataMapper.insert(livingChatData);
                }catch (Exception e1){
                    logger.error("批量插入出错，已改为单个插入，错误数据：");
                    logger.error(livingChatData.toString());
                    logger.error(e1.getMessage());
                }
            }
        }finally {
            if(sqlSession != null)
                sqlSession.close();
        }
    }


    /**
     * 从json文件导入弹幕数据（暂时不需要了）
     * @param file
     * @param liveDate
     * @return
     */
    public String importJsonFile(MultipartFile file, String liveDate) {
        long startTime = System.currentTimeMillis();   //获取开始时间
        byte[] bytes = null;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            return "Json文件错误";
        }
        String jsonData = new String(bytes);
        JSONArray jsonArray = JSON.parseArray(jsonData);
        List<LiveChatData> batchList = new ArrayList<>();
        for(int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            LiveChatData dto = JsonUtil.getLiveChat(jsonObject);
            dto.setLiveDate(liveDate);
            String message = dto.getMessage();
            if(message == null){
                logger.error("jsonArray 第" + i + "条数据有误，内容：");
                logger.error(jsonObject.toString());
                continue;
            }
            JSONArray emotes = (JSONArray) jsonObject.get("emotes");
            if(emotes != null && emotes.size() > 0){
                //包含emoji
                List<EmotesData> emotesDataList = JsonUtil.getEmotes(emotes);
                for (EmotesData emotesData :  emotesDataList) {
                    emotesDataService.insertNotExists(emotesData);
                }
                dto.setEmotesCount(emotes.size());
            }
            batchList.add(dto);
            if(batchList.size() >= Constant.BATCH_IMPORT_SIZE){
                this.insertBatch(batchList);
                batchList.clear();
            }
        }
        if(batchList.size() > 0){
            this.insertBatch(batchList);
            batchList.clear();
        }
        if(((JSONObject)jsonArray.get(0)).get("time_in_seconds") == null){
            //用于直播中下载弹幕Json文件的情况，此时Json数据中没有timeInSeconds字段，会报错
            Long startTimestamp = this.selectStartTimestamp(liveDate);
            if(startTimestamp != null && startTimestamp > 0){
                this.updateTimestamp(liveDate,startTimestamp);
            }
        }
        int count = selectCount(liveDate);
        StringBuilder result = new StringBuilder();
        result.append(liveDate);
        result.append(" json数据总条数：");
        result.append(jsonArray.size());
        result.append("。导入后条数：");
        result.append(count);
        result.append(" 。用时：");
        long time = System.currentTimeMillis() - startTime; //获取结束时间
        result.append(time / 1000);
        result.append("秒");
        return result.toString();
    }
}
