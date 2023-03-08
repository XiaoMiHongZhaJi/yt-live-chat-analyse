package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.dao.LiveChatDataMapper;
import com.lwf.ytlivechatanalyse.dao.LiveInfoMapper;
import com.lwf.ytlivechatanalyse.dao.LivingChatDataMapper;
import com.lwf.ytlivechatanalyse.util.CmdUtil;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.CurlUtil;
import com.lwf.ytlivechatanalyse.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LiveInfoService {

    private final Logger logger = LoggerFactory.getLogger(LiveInfoService.class);

    @Autowired
    LiveInfoMapper liveInfoMapper;

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
        queryWrapper.like("live_date", liveDate);
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
        queryWrapper.select("live_date", "title", "url", "id", "live_status", "download_status");
        if(liveInfo.getSrtCount() != null && liveInfo.getSrtCount() > 0){
            queryWrapper.gt("srt_count", 0);
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
        return liveInfoMapper.selectOne(queryWrapper);
    }

    public List<LiveInfo> selectList(){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.orderByDesc("live_date");
        List<LiveInfo> liveInfoList = liveInfoMapper.selectList(queryWrapper);
        return liveInfoList;
    }

    public int updateLiveInfoById(LiveInfo liveInfo){
        liveInfo.setUpdateTime(new Date());
        return liveInfoMapper.updateById(liveInfo);
    }

    public int updateLiveInfoByDate(LiveInfo liveInfo){
        liveInfo.setUpdateTime(new Date());
        UpdateWrapper<LiveInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("live_date", liveInfo.getLiveDate());
        return liveInfoMapper.update(liveInfo, updateWrapper);
    }

    public void addLiveInfo(LiveInfo liveInfo, boolean downLiveChat, boolean getLiveInfo) {
        String url = liveInfo.getUrl();
        if(url.contains("youtube")){
            liveInfo.setPlatform("y");
            if(getLiveInfo){
                //补全信息
                Map<String, String> info = CurlUtil.getLiveInfo(liveInfo.getUrl());
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
                        liveInfo.setStartTimestamp(DateUtil.getTimestamp(startTimestamp) * 1000);
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
                if(url.contains("luoshushu")){
                    liveInfo.setLiveDate(DateUtil.getNowDate() + "_l");
                }
            }
        }
        if(StringUtils.isBlank(liveInfo.getTitle())){
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
        String ps = CmdUtil.chatDownloaderPs(url);
        if(StringUtils.isNotBlank(ps)){
            logger.info("url 已在下载。当前时间：" + DateUtil.getNowDateTime());
            return ;
        }
        if("t".equals(liveInfo.getPlatform())){
            // twitch
            new Thread(() -> {
                String fileName = DateUtil.getNowDateTime() + "_t_living.json";
                CmdUtil.chatDownloader(url, fileName);
                updateLiveInfo.setLivingChatCount(liveChatDataService.selectLivingCount(liveInfo.getLiveDate()));
                updateLiveInfo.setLiveStatus(LiveInfo.LIVE_STATUS_DONE);
                updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
                updateLiveInfoById(updateLiveInfo);
                logger.info("twitch 直播已结束，下载弹幕信息完成。当前时间：" + DateUtil.getNowDateTime());
            }).start();
        }else if(LiveInfo.LIVE_STATUS_DONE.equals(liveInfo.getLiveStatus())){
            //直播已结束，录像弹幕
            liveChatDataService.deleteByLiveDate(liveDate);
            new Thread(() -> {
                CmdUtil.chatDownloader(url, liveDate + ".json");
                updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
                updateLiveInfo.setLiveChatCount(liveChatDataService.selectCount(liveInfo.getLiveDate()));
                updateLiveInfoById(updateLiveInfo);
                logger.info("youtube 下载弹幕信息完成。当前时间：" + DateUtil.getNowDateTime());
            }).start();
        }else{
            //直播中或预告状态
            new Thread(() ->{
                for (int i = 0; i < Constant.DOWNLOAD_FAILURE_RETRY_COUNT; i++) {
                    String fileName = DateUtil.getNowDateTime() + "_living.json";
                    CmdUtil.chatDownloader(url, fileName);
                    //命令行结束，判断直播是否结束
                    Map<String, String> newInfo = CurlUtil.getLiveInfo(url);
                    String newLiveStatus = newInfo.get("liveStatus");
                    updateLiveInfo.setLiveStatus(newLiveStatus);
                    updateLiveInfo.setLivingChatCount(liveChatDataService.selectLivingCount(liveInfo.getLiveDate()));
                    if(StringUtils.isBlank(newLiveStatus)){
                        logger.error("youtube 获取直播信息失败。当前时间：" + DateUtil.getNowDateTime());
                        updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_NONE);
                        updateLiveInfoById(updateLiveInfo);
                        break;
                    }else if(LiveInfo.LIVE_STATUS_DONE.equals(newLiveStatus)){
                        //直播结束，更新状态
                        updateLiveInfo.setViewCount(Integer.valueOf(newInfo.get("viewCount")));
                        updateLiveInfo.setLikeCount(newInfo.get("likeCount"));
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
        }
    }
}
