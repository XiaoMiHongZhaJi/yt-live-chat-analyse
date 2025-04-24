package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lwf.ytlivechatanalyse.bean.*;
import com.lwf.ytlivechatanalyse.dao.LiveChatDataMapper;
import com.lwf.ytlivechatanalyse.dao.LiveInfoLogMapper;
import com.lwf.ytlivechatanalyse.dao.LiveInfoMapper;
import com.lwf.ytlivechatanalyse.dao.LivingChatDataMapper;
import com.lwf.ytlivechatanalyse.interceptor.DynamicSchemaInterceptor;
import com.lwf.ytlivechatanalyse.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Service
public class LiveInfoService {

    private final Logger logger = LoggerFactory.getLogger(LiveInfoService.class);

    @Autowired
    LiveInfoMapper liveInfoMapper;

    @Autowired
    LiveInfoLogMapper liveInfoLogMapper;

    @Autowired
    LiveChatDataMapper liveChatDataMapper;

    @Autowired
    LivingChatDataMapper livingChatDataMapper;

    @Autowired
    LiveChatDataService liveChatDataService;

    public void insertLiveInfo(LiveInfo liveInfo) {
        String liveDate = liveInfo.getLiveDate();
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        liveInfoMapper.insert(liveInfo);
    }

    public List<LiveInfo> queryListBySelector(LiveInfo liveInfo){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.orderByDesc("live_date");
        queryWrapper.select("id", "live_date", "title", "url", "live_status", "download_status", "start_timestamp");
        if(liveInfo.getSrtCount() != null && liveInfo.getSrtCount() > 0){
            queryWrapper.gt("srt_count", 0);
        }
        String liveDate = liveInfo.getLiveDate();
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return liveInfoMapper.selectList(queryWrapper);
    }

    public List<LiveInfo> queryListById(String ids){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.orderByDesc("live_date");
        if(StringUtils.isNotBlank(ids)){
            queryWrapper.in("id", ids.split(","));
        }
        return liveInfoMapper.selectList(queryWrapper);
    }

    public LiveInfo selectOne(LiveInfo liveInfo){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        if(liveInfo.getId() != null){
            queryWrapper.eq("id", liveInfo.getId());
        }else {
            if (StringUtils.isNotBlank(liveInfo.getLiveDate())) {
                queryWrapper.eq("live_date", liveInfo.getLiveDate());
            }
            if (StringUtils.isNotBlank(liveInfo.getUrl())) {
                queryWrapper.eq("url", liveInfo.getUrl());
            }
        }
        queryWrapper.last("limit 1");
        String liveDate = liveInfo.getLiveDate();
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return liveInfoMapper.selectOne(queryWrapper);
    }

    public List<LiveInfo> selectList(String year){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.orderByDesc("live_date");
        queryWrapper.select("id", "live_date", "title", "url", "live_status", "download_status", "view_count", "like_count", "srt_count", "live_chat_count", "living_chat_count", "platform", "start_timestamp");
        if(StringUtils.isNotBlank(year) && !year.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + year);
        }
        List<LiveInfo> liveInfoList = liveInfoMapper.selectList(queryWrapper);
        return liveInfoList;
    }

    public int updateLiveInfoById(LiveInfo liveInfo){
        liveInfo.setUpdateTime(new Date());
        String liveDate = liveInfo.getLiveDate();
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return liveInfoMapper.updateById(liveInfo);
    }

    public int updateLiveInfoByDate(LiveInfo liveInfo){
        liveInfo.setUpdateTime(new Date());
        UpdateWrapper<LiveInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("live_date", liveInfo.getLiveDate());
        String liveDate = liveInfo.getLiveDate();
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return liveInfoMapper.update(liveInfo, updateWrapper);
    }

    public String addYoutubeLiveInfo(LiveInfo liveInfo, boolean downLiveChat, boolean getLiveInfo) {
        String url = liveInfo.getUrl();
        liveInfo.setPlatform("y");
        if(getLiveInfo){
            //补全信息
            Map<String, String> info = CurlUtil.getLiveInfo(liveInfo.getUrl());
            addLiveInfoLog(url, info);
            if(StringUtils.isNotBlank(info.get("viewCount"))){
                liveInfo.setViewCount(Integer.valueOf(info.get("viewCount")));
            }
            if(StringUtils.isNotBlank(info.get("likeCount"))){
                liveInfo.setLikeCount(info.get("likeCount"));
            }
            if(StringUtils.isBlank(liveInfo.getTitle())){
                liveInfo.setTitle(info.get("title"));
            }
            if(StringUtils.isBlank(liveInfo.getLiveStatus())){
                liveInfo.setLiveStatus(info.get("liveStatus"));
            }
            if(StringUtils.isBlank(liveInfo.getLiveDate())){
                liveInfo.setLiveDate(info.get("liveDate"));
            }
            if(liveInfo.getStartTimestamp() == null && LiveInfo.LIVE_STATUS_LIVEING.equals(liveInfo.getLiveStatus())){
                String startTimestamp = info.get("startTimestamp");
                if(StringUtils.isNotBlank(startTimestamp)){
                    liveInfo.setStartTimestamp(Long.valueOf(startTimestamp));
                }
            }
        }
        if(StringUtils.isBlank(liveInfo.getLiveDate())){
            liveInfo.setLiveDate(DateUtil.getNowDate());
        }
        if(StringUtils.isBlank(liveInfo.getTitle())){
            liveInfo.setTitle(url.substring(url.lastIndexOf("=") + 1));
        }
        String liveDate = liveInfo.getLiveDate();
        // 判断 url 是否已存在，若已存在，则不再添加
        Long urlCount = getUrlCount(url, liveDate);
        if(urlCount > 0){
            logger.warn("{} url 已存在，不再添加", url);
            return "url 已存在";
        }
        // 判断 Title 是否已存在，若已存在，则重命名
        Long titleCount = getTitleCount(liveDate);
        int i = 0;
        while (titleCount > 0){
            i ++;
            if(i >= 10){
                logger.error("{} Title 已存在，尝试次数过多", liveDate);
                return "Title 已存在，尝试次数过多";
            }
            logger.warn("{} Title 已存在，尝试更换", liveDate);
            liveDate = String.format("%s_0%s", liveInfo.getLiveDate(), i);
            liveInfo.setLiveDate(liveDate);
            titleCount = getTitleCount(liveDate);
        }
        insertLiveInfo(liveInfo);
        if(downLiveChat){
            downloadChatData(liveInfo);
        }
        return null;
    }

    public String addTwitchLiveInfo(LiveInfo liveInfo, boolean downLiveChat) {
        String url = liveInfo.getUrl();
        // twitch平台
        liveInfo.setPlatform("t");
        liveInfo.setLiveStatus(LiveInfo.LIVE_STATUS_PREVIEW);
        // 补全信息
        if(StringUtils.isBlank(liveInfo.getLiveDate())) {
            liveInfo.setLiveDate(DateUtil.getNowDate() + "_t");
            if(url.contains("luoshushu")){
                liveInfo.setLiveDate(DateUtil.getNowDate() + "_l");
            }
        }
        if(StringUtils.isBlank(liveInfo.getTitle())){
            liveInfo.setTitle(url.substring(url.lastIndexOf("/") + 1));
        }
        String liveDate = liveInfo.getLiveDate();
        // 判断 Title 是否已存在，若已存在，则不再添加
        Long titleCount = getTitleCount(liveDate);
        if (titleCount == 0){
            insertLiveInfo(liveInfo);
        }
        if(downLiveChat){
            downloadChatData(liveInfo);
        }
        return null;
    }

    private Long getUrlCount(String url, String liveDate) {
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.eq("url", url);
        if(!liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return liveInfoMapper.selectCount(queryWrapper);
    }

    private Long getTitleCount(String liveDate) {
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.eq("live_date", liveDate);
        if(!liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return liveInfoMapper.selectCount(queryWrapper);
    }

    public void downloadChatData(LiveInfo liveInfo) {
        String url = liveInfo.getUrl();
        Integer id = liveInfo.getId();
        String liveDate = liveInfo.getLiveDate();
        if(StringUtils.isBlank(url) || id == null || id == 0){
            return ;
        }
        //状态更新为下载中
        LiveInfo updateLiveInfo = new LiveInfo();
        updateLiveInfo.setId(id);
        updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DOWNLOADING);
        updateLiveInfoById(updateLiveInfo);
        // 检查是否已经在下载
        if("y".equals(liveInfo.getPlatform()) && LiveInfo.DOWNLOAD_STATUS_DOWNLOADING.equals(liveInfo.getDownloadStatus())){
            logger.info("{} 已在下载。当前时间：{}", url, DateUtil.getNowDateTime());
            return ;
        }
        if("t".equals(liveInfo.getPlatform())){
            // twitch
            new Thread(() -> {
                String fileName = DateUtil.getNowDateTime() + "_t_living.json";
                CmdUtil.chatDownloader(url, liveDate, fileName);
                updateLiveInfo.setLivingChatCount(liveChatDataService.selectLivingCount(liveInfo.getLiveDate()));
                updateLiveInfo.setLiveStatus(LiveInfo.LIVE_STATUS_DONE);
                updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
                updateLiveInfoById(updateLiveInfo);
                logger.info("twitch 直播已结束，下载弹幕信息完成。当前时间：{}", DateUtil.getNowDateTime());
            }).start();
        }else if(LiveInfo.LIVE_STATUS_DONE.equals(liveInfo.getLiveStatus())){
            //直播已结束，录像弹幕
            new Thread(() -> {
                CmdUtil.chatDownloader(url, liveDate, liveDate + ".json");
                int count = liveChatDataService.selectCount(liveInfo.getLiveDate());
                if(count == 0){
                    logger.error("{} 下载弹幕信息失败，条数为0，url：{}", liveDate, url);
                    updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_FILURE);
                    updateLiveInfoById(updateLiveInfo);
                    return;
                }
                updateLiveInfo.setLiveChatCount(count);
                logger.info("{} 下载弹幕信息完成，条数：{}", liveDate, count);
                Integer asyncCount = liveChatDataMapper.asyncLivingChatData(liveDate);
                logger.info("{} 同步弹幕信息完成，同步条数：{}", liveDate, asyncCount);
                updateLiveInfo.setLivingChatCount(liveChatDataService.selectCount(liveInfo.getLiveDate()));
                Long startTimestamp = liveChatDataService.selectStartTimestamp(liveDate);
                if(startTimestamp != null){
                    updateLiveInfo.setStartTimestamp(startTimestamp);
                }
                updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
                updateLiveInfoById(updateLiveInfo);
            }).start();
        }else{
            //直播中或预告状态
            new Thread(() ->{
                for (int i = 0; i < Constant.DOWNLOAD_FAILURE_RETRY_COUNT; i++) {
                    String fileName = DateUtil.getNowDateTime() + "_living.json";
                    CmdUtil.chatDownloader(url, liveDate, fileName);
                    //命令行结束，判断直播是否结束
                    Map<String, String> newInfo = CurlUtil.getLiveInfo(url);
                    String newLiveStatus = newInfo.get("liveStatus");
                    updateLiveInfo.setLiveStatus(newLiveStatus);
                    updateLiveInfo.setLivingChatCount(liveChatDataService.selectLivingCount(liveInfo.getLiveDate()));
                    addLiveInfoLog(url, newInfo);
                    if(StringUtils.isBlank(newLiveStatus)){
                        logger.error("youtube 获取直播信息失败。当前时间：" + DateUtil.getNowDateTime());
                        updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_NONE);
                        updateLiveInfoById(updateLiveInfo);
                        break;
                    }else if(LiveInfo.LIVE_STATUS_DONE.equals(newLiveStatus)){
                        //直播结束，更新状态
                        updateLiveInfo.setViewCount(Integer.valueOf(newInfo.get("viewCount")));
                        updateLiveInfo.setLikeCount(newInfo.get("likeCount"));
                        updateLiveInfo.setDurationTime(newInfo.get("videoDurationTime"));
                        updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
                        updateLiveInfoById(updateLiveInfo);
                        logger.info("youtube 直播已结束，下载弹幕信息完成。当前时间：" + DateUtil.getNowDateTime());
                        break;
                    }else if(LiveInfo.LIVE_STATUS_LIVEING.equals(newLiveStatus)){
                        logger.error("youtube 下载出错，第" + (i + 1) + "次，正在重试。当前时间：" + DateUtil.getNowDateTime());
                        if(i == Constant.DOWNLOAD_FAILURE_RETRY_COUNT - 1){
                            //多次失败
                            updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_FILURE);
                            updateLiveInfoById(updateLiveInfo);
                            logger.error("youtube 下载失败，重试次数" + Constant.DOWNLOAD_FAILURE_RETRY_COUNT + "。当前时间：" + DateUtil.getNowDateTime());
                        }
                    }
                }
            }).start();
            new Thread(()->{
                try {
                    Thread.sleep(60 * 1000);
                    LiveInfoLog latestLog = queryLatestLog(url, null);
                    if(latestLog != null){
                        Long lastUpdateTimestamp = latestLog.getUpdateTimestamp();
                        Long nowTimestamp = DateUtil.getNowTimestamp();
                        long second = (nowTimestamp - lastUpdateTimestamp) / 1000000;
                        if(second <= 58l){
                            logger.info("已有InfoLog正在运行，最近更新时间：{}", latestLog.getUpdateTime());
                            return;
                        }
                    }
                    while (Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).get(Calendar.HOUR_OF_DAY) != 3){
                        Map<String, String> newInfo = CurlUtil.getLiveInfo(url);
                        Thread.sleep(60 * 1000);
                        addLiveInfoLog(url, newInfo);
                        if("2".equals(newInfo.get("liveStatus"))){
                            break;
                        }
                    }
                }catch(Exception e){
                    logger.error("更新LiveInfoLog出错：", e);
                }
            }).start();
        }
    }

    public LiveInfoLog queryLatestLog(String url, String liveDate){
        QueryWrapper<LiveInfoLog> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isBlank(url)){
            queryWrapper.eq("live_date", liveDate);
        }else{
            queryWrapper.eq("url", url);
        }
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit 1");
        LiveInfoLog liveInfoLog = liveInfoLogMapper.selectOne(queryWrapper);
        return liveInfoLog;
    }

    public void addLiveInfoLog(String url, Map<String, String> info){
        String liveDate = info.get("liveDate");
        LiveInfoLog liveInfoLog = new LiveInfoLog();
        liveInfoLog.setUrl(url);
        liveInfoLog.setTitle(info.get("title"));
        liveInfoLog.setLiveDate(liveDate);
        liveInfoLog.setLiveStatus(info.get("liveStatus"));
        liveInfoLog.setLikeCount(info.get("likeCount"));
        String viewCount = info.get("viewCount");
        if(StringUtils.isNumeric(viewCount)){
            liveInfoLog.setViewCount(Integer.parseInt(viewCount));
        }
        String livingViewCount = info.get("livingViewCount");
        if(StringUtils.isNumeric(livingViewCount)){
            liveInfoLog.setLivingViewCount(Integer.parseInt(livingViewCount));
        }
        liveInfoLog.setLivingChatCount(liveChatDataService.selectLivingCount(liveDate));
        liveInfoLog.setPlatform(url.contains("youtube") ? "y" : "t");
        liveInfoLog.setUpdateTimestamp(DateUtil.getNowTimestamp());
        liveInfoLog.setUpdateTime(new Date());
        liveInfoLogMapper.insert(liveInfoLog);
    }

    public List<LiveChatData> getLiveChatData(String liveDate) {
        logger.info("开始获取弹幕数据");
        LiveChatData liveChatData = new LiveChatData();
        liveChatData.setLiveDate(liveDate);
        List<LiveChatData> chatList = liveChatDataService.selectList(liveChatData, true);
        if(CollectionUtils.isEmpty(chatList)){
            List<LivingChatData> livingChatData = liveChatDataService.selectLivingList(liveChatData, true);
            chatList.addAll(livingChatData);
        }
        return chatList;
    }

    public List<String> getBulletContent(List<LiveChatData> chatList, Long startTimestamp, BulletConfig config) {
        logger.info("开始生成弹幕ass文件：{} {}", startTimestamp, config);
        return BulletAssUtil.getAssContent(chatList, startTimestamp, config);
    }

    public File getBulletFile(String bulletContent, String fileName) {
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
        File file = new File(filePath + fileName);
        if(file.exists()){
            return file;
        }
        BufferedWriter writer = null;
        try{
            writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8));
            writer.write(bulletContent);
            writer.flush();
        }catch(Exception e){
            logger.error("生成弹幕文件失败", e);
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
}
