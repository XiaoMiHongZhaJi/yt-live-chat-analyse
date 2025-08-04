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
import com.lwf.ytlivechatanalyse.interceptor.DynamicSchemaInterceptor;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
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
        String keywords = liveChatData.getMessage();
        if(StringUtils.isNotBlank(keywords)){
            queryWrapper.like("message", keywords);
        }
        String authorName = liveChatData.getAuthorName();
        if(StringUtils.isNotBlank(authorName)){
            int index = authorName.indexOf("「");
            if(index > -1){
                String lastAuthorName = authorName.substring(0, index);
                String firstAuthorName = authorName.substring(index + 1, authorName.length() - 1);
                queryWrapper.and(wrapper -> wrapper
                    .like("author_name", lastAuthorName)
                    .or()
                    .like("author_name", firstAuthorName)
                );
            }else{
                queryWrapper.like("author_name", authorName);
            }
        }
        String liveDate = liveChatData.getLiveDate();
        if(StringUtils.isNotBlank(liveDate)){
            queryWrapper.likeRight("live_date", liveDate);
        }
        queryWrapper.eq("blocked", 0);
        queryWrapper.orderBy(true, isAsc, "timestamp");
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return liveChatDataMapper.selectList(queryWrapper);
    }

    public List<LivingChatData> selectLivingList(LiveChatData liveChatData, boolean isAsc){
        QueryWrapper<LivingChatData> queryWrapper = new QueryWrapper<>();
        String keywords = liveChatData.getMessage();
        if(StringUtils.isNotBlank(keywords)){
            queryWrapper.like("message", keywords);
        }
        String authorName = liveChatData.getAuthorName();
        if(StringUtils.isNotBlank(authorName)){
            int index = authorName.indexOf("「");
            if(index > -1){
                String lastAuthorName = authorName.substring(0, index);
                String firstAuthorName = authorName.substring(index + 1, authorName.length() - 1);
                queryWrapper.and(wrapper -> wrapper
                    .like("author_name", lastAuthorName)
                    .or()
                    .like("author_name", firstAuthorName)
                );
            }else{
                queryWrapper.like("author_name", authorName);
            }
        }
        String liveDate = liveChatData.getLiveDate();
        if(StringUtils.isNotBlank(liveDate)){
            queryWrapper.likeRight("live_date", liveDate);
        }
        queryWrapper.eq("blocked", 0);
        queryWrapper.orderBy(true, isAsc, "timestamp");
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return livingChatDataMapper.selectList(queryWrapper);
    }

    public Long selectStartTimestamp(String liveDate){
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        LiveChatData liveChatData = liveChatDataMapper.selectStartMessage(liveDate);
        if(liveChatData == null){
            return null;
        }
        Long timestamp = liveChatData.getTimestamp();
        BigDecimal timeInSeconds = liveChatData.getTimeInSeconds();
        if(timestamp == null || timestamp == 0L || timeInSeconds == null){
            return null;
        }
        return timestamp - (long)(timeInSeconds.doubleValue() * 1000000);
    }

    public int selectCount(String liveDate){
        //录像
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("live_date", liveDate);
        queryWrapper.eq("blocked", 0);
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return Math.toIntExact(liveChatDataMapper.selectCount(queryWrapper));
    }

    public int selectLivingCount(String liveDate){
        //直播中，直播预告
        QueryWrapper<LivingChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("live_date", liveDate);
        queryWrapper.eq("blocked", 0);
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return Math.toIntExact(livingChatDataMapper.selectCount(queryWrapper));
    }

    public void insertBatch(List<LiveChatData> batchList){
        if (CollectionUtils.isEmpty(batchList)) {
            return;
        }
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            String liveDate = batchList.get(0).getLiveDate();
            if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
                DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
            }
            for (LiveChatData liveChatData : batchList) {
                sqlSession.insert("com.lwf.ytlivechatanalyse.dao.LiveChatDataMapper.insertNotExists", liveChatData);
            }
            sqlSession.commit();
        } catch (Exception e) {
            for (LiveChatData liveChatData : batchList) {
                String liveDate = liveChatData.getLiveDate();
                if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
                    DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
                }
                try {
                    liveChatDataMapper.insertNotExists(liveChatData);
                } catch (Exception e1) {
                    logger.error("批量插入出错，已改为单个插入，错误数据：", e1);
                    logger.error(liveChatData.toString());
                }
            }
        }
    }

    public String getJsonString(LiveChatData liveChatData){
        List<LiveChatData> liveChatAll = selectList(liveChatData, true);
        List<Map<String,Object>> tempList = new ArrayList<>();
        for (LiveChatData liveChat : liveChatAll) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("authorImage", liveChat.getAuthorImage());
            temp.put("authorName", liveChat.getAuthorName());
            temp.put("emotesCount", liveChat.getEmotesCount());
            temp.put("message", liveChat.getMessage());
            temp.put("timeText", liveChat.getTimeText());
            temp.put("timeInSeconds", liveChat.getTimeInSeconds());
            tempList.add(temp);
        }
        return JSON.toJSONString(tempList);
    }

    public void deleteByLiveDate(String liveDate){
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveDate);
        liveChatDataMapper.delete(queryWrapper);
    }

    public List<LiveChatData> selectLiveChatDetail(LiveChatData liveChatData, int limit) {
        String liveDate = liveChatData.getLiveDate();
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        Integer id = liveChatData.getId();
        if(id == null){
            id = 0;
        }
        int startId = id - limit / 4;
        int endId = startId + limit;
        queryWrapper.ge("id", startId);
        queryWrapper.lt("id", endId);
        queryWrapper.orderByAsc("id");
        queryWrapper.last("limit " + limit);
        if(StringUtils.isNotBlank(liveDate)){
            queryWrapper.likeRight("live_date", liveDate);
            if(!liveDate.startsWith(Constant.DEFAULT_YEAR)){
                DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
            }
        }
        return liveChatDataMapper.selectList(queryWrapper);
    }


    /**
     * 从json文件导入弹幕数据
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
        int count = selectCount(liveDate);
        long time = System.currentTimeMillis() - startTime; //获取结束时间
        return String.format("%s json数据总条数：%d。导入条数：%d。用时：%d秒", liveDate, jsonArray.size(), count, time / 1000);
    }
}
