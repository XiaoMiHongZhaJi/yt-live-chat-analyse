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

import java.io.File;
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

    public void insertOrUpdate(LiveInfo liveInfo) {
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        String liveDate = liveInfo.getLiveDate();
        int index = liveDate.indexOf("_");
        if(index > -1){
            liveDate = liveDate.substring(0, index);
        }
        queryWrapper.likeRight("live_date", liveDate);
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        List<LiveInfo> liveInfoList = liveInfoMapper.selectList(queryWrapper);
        if (liveInfoList.size() == 0){
            liveInfoMapper.insert(liveInfo);
        }else{
            liveInfo.setId(liveInfoList.get(0).getId());
            if("y".equals(liveInfo.getPlatform())){
                liveInfoMapper.updateById(liveInfo);
            }
        }
    }

    public List<LiveInfo> queryListBySelector(LiveInfo liveInfo){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.orderByDesc("live_date");
        queryWrapper.select("live_date", "title", "url", "id", "live_status", "download_status", "start_timestamp");
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

    public void addLiveInfo(LiveInfo liveInfo, boolean downLiveChat, boolean getLiveInfo) {
        String url = liveInfo.getUrl();
        if(url != null && url.contains("youtube")){
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
        }else{
            liveInfo.setPlatform("t");
            liveInfo.setLiveStatus(LiveInfo.LIVE_STATUS_PREVIEW);
            if(StringUtils.isBlank(liveInfo.getLiveDate())) {
                liveInfo.setLiveDate(DateUtil.getNowDate() + "_t");
                if(url != null && url.contains("luoshushu")){
                    liveInfo.setLiveDate(DateUtil.getNowDate() + "_l");
                }
            }
        }
        if(url != null && StringUtils.isBlank(liveInfo.getTitle())){
            liveInfo.setTitle(url.substring(url.lastIndexOf("/") + 1));
        }
        insertOrUpdate(liveInfo);
        if(downLiveChat){
            downloadChatData(liveInfo);
        }
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

    public Result getBulletFile(String liveDate, String startTime, BulletConfig config) {
        Long startTimestamp = null;
        if(StringUtils.isBlank(startTime)){
            LiveInfo liveInfo = new LiveInfo();
            liveInfo.setLiveDate(liveDate);
            liveInfo = selectOne(liveInfo);
            if(liveInfo != null){
                startTimestamp = liveInfo.getStartTimestamp();
            }
            logger.info("获取到开播信息中的startTimestamp：{}", startTimestamp);
        }else if(startTime.startsWith("1") && StringUtils.isNumeric(startTime)){
            //时间戳
            startTime = String.format("%-16d", startTime).replace(" ", "0");
            startTimestamp = Long.parseLong(startTime);
        }else if(startTime.startsWith("202")){
            //日期时间
            startTimestamp = DateUtil.getTimestamp(startTime);
            logger.info("获取到转换的startTimestamp：{}", startTimestamp);
        }else if(startTime.startsWith("20:") && startTime.length() <= 8){
            //时间
            startTimestamp = DateUtil.getTimestamp(liveDate + startTime);
            logger.info("获取到转换的startTimestamp：{}", startTimestamp);
        }else{
            logger.warn("输入的开播时间有误：{} {}", liveDate, startTime);
            return new Result(500, "输入的开播时间有误");
        }
        logger.info("开始获取弹幕数据");
        LiveChatData liveChatData = new LiveChatData();
        liveChatData.setLiveDate(liveDate);
        List<LiveChatData> chatList = liveChatDataService.selectList(liveChatData, true);
        if(CollectionUtils.isEmpty(chatList)){
            List<LivingChatData> livingChatData = liveChatDataService.selectLivingList(liveChatData, true);
            chatList.addAll(livingChatData);
        }
        if(CollectionUtils.isEmpty(chatList)){
            logger.warn("所选日期无弹幕数据：{} {}", liveDate, startTime);
            return new Result(500, "所选日期无弹幕数据");
        }
        logger.info("开始生成弹幕ass文件：{} {}", liveDate, startTime);
        if(config == null){
            config = new BulletConfig();
        }
        return BulletAssUtil.getAssFile(chatList, startTimestamp, config);
    }
}
