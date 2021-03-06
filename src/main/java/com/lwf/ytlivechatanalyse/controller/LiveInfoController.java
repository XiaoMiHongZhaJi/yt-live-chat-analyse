package com.lwf.ytlivechatanalyse.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.service.LiveChatDataService;
import com.lwf.ytlivechatanalyse.service.LiveInfoService;
import com.lwf.ytlivechatanalyse.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/liveInfo")
public class LiveInfoController {

    private final Logger logger = LoggerFactory.getLogger(LiveInfoController.class);

    @Autowired
    LiveChatDataService liveChatDataService;

    @Autowired
    LiveInfoService liveInfoService;

    @PostMapping("/addLiveInfo")
    public int addLiveInfo(LiveInfo liveInfo, boolean downLiveChat, boolean getLiveInfo, MultipartFile file){
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
        }
        if(StringUtils.isBlank(liveInfo.getLiveDate())){
            liveInfo.setLiveDate(DateUtil.getNowDate());
        }
        liveInfoService.insertOrUpdate(liveInfo);
        if(downLiveChat){
            downloadChatData(liveInfo);
        }
        //从文件导入
        if(file != null){
            String filename = file.getOriginalFilename();
            if(StringUtils.isBlank(filename)){
                if(filename.endsWith(".xml")){

                }
            }
        }
        return 1;
    }

    @RequestMapping("/queryListBySelector")
    public List<LiveInfo> queryListBySelector(){
        return liveInfoService.queryListBySelector();
    }

    @RequestMapping("/queryListById")
    public List<LiveInfo> queryListById(String ids){
        //用于定时器更新
        List<LiveInfo> liveInfoList = liveInfoService.queryListById(ids);
        for(LiveInfo liveInfo : liveInfoList){
            if(LiveInfo.DOWNLOAD_STATUS_DOWNLOADING.equals(liveInfo.getDownloadStatus())){
                //下载中
                liveInfo.setChatCount(liveChatDataService.selectCount(liveInfo));
                LiveInfo updateLiveInfo = new LiveInfo();
                updateLiveInfo.setId(liveInfo.getId());
                updateLiveInfo.setChatCount(liveInfo.getChatCount());
                updateLiveInfo.setUpdateTime(new Date());
                liveInfoService.updateLiveInfoById(updateLiveInfo);
            }
        }
        return liveInfoList;
    }

    @RequestMapping("/queryList")
    public Result<LiveInfo> queryList(int page, int limit){
        limit = limit > Constant.MAX_PAGE_SIZE ? Constant.MAX_PAGE_SIZE : limit;
        PageHelper.startPage(page, limit);
        return new Result<>(new PageInfo<>(liveInfoService.selectList()));
    }

    @RequestMapping("/queryLastLiveInfo")
    public String queryLastLiveDate(String liveDate){
        Date parseDate = new Date();
        try {
            parseDate = DateUtils.parseDate(liveDate.substring(0, 10), "yyyy-MM-dd");
        }catch (Exception e){
            logger.error("转换日期失败，" + liveDate);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseDate);
        calendar.add(Calendar.DATE, -7);
        String lastLiveDate = DateFormatUtils.format(calendar, "yyyy-MM-dd");
        return lastLiveDate;
    }

    @RequestMapping("/queryLiveInfo")
    public LiveInfo queryLiveInfo(LiveInfo liveInfo){
        return liveInfoService.selectOne(liveInfo);
    }

    @RequestMapping("/updateLiveInfo")
    public void updateLiveInfo(LiveInfo liveInfo){
        if(liveInfo == null || liveInfo.getId() == null){
            throw new RuntimeException("更新失败");
        }
        liveInfo.setUpdateTime(new Date());
        liveInfoService.updateLiveInfoById(liveInfo);
    }

    @RequestMapping("/getLiveInfo")
    public Map<String,String> getLiveInfo(String url){
        Map<String, String> info = CurlUtil.getLiveInfo(url);
        return info;
    }

    @RequestMapping("/downloadChatData")
    public String downloadChatData(LiveInfo liveInfo){
        String url = liveInfo.getUrl();
        Integer id = liveInfo.getId();
        String liveDate = liveInfo.getLiveDate();
        if(StringUtils.isBlank(url) || id == null || id == 0){
            return "0";
        }
        //状态更新为下载中
        LiveInfo updateLiveInfo = new LiveInfo();
        updateLiveInfo.setId(id);
        updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DOWNLOADING);
        updateLiveInfo.setUpdateTime(new Date());
        liveInfoService.updateLiveInfoById(updateLiveInfo);
        if(LiveInfo.LIVE_STATUS_DONE.equals(liveInfo.getLiveStatus())){
            //直播已结束，重新下载，先删除已有的
            liveChatDataService.deleteByLiveDate(liveDate);
            new Thread(() -> {
                CmdUtil.execCmd("chat_downloader " + url + " --output output/" + liveDate + ".json", true, false);
                updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
                updateLiveInfo.setChatCount(liveChatDataService.selectCount(liveInfo));
                updateLiveInfo.setUpdateTime(new Date());
                liveInfoService.updateLiveInfoById(updateLiveInfo);
                logger.info("下载弹幕信息完成。当前时间：" + DateUtil.getNowDateTime());
            }).start();
        }else{
            //直播中或预告状态
            new Thread(() ->{
                for (int i = 0; i < Constant.DOWNLOAD_FAILURE_RETRY_COUNT; i++) {
                    String fileName = DateUtil.getNowDateTime() + "_living.json";
                    CmdUtil.execCmd("chat_downloader " + url + " --output output/" + fileName, true, false);
                    //命令行结束，判断直播是否结束
                    Map<String, String> newInfo = CurlUtil.getLiveInfo(url);
                    String newLiveStatus = newInfo.get("liveStatus");
                    updateLiveInfo.setLiveStatus(newLiveStatus);
                    updateLiveInfo.setChatCount(liveChatDataService.selectCount(liveInfo));
                    updateLiveInfo.setUpdateTime(new Date());
                    if(StringUtils.isBlank(newLiveStatus)){
                        logger.error("获取直播信息失败。当前时间：" + DateUtil.getNowDateTime());
                        updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_NONE);
                        liveInfoService.updateLiveInfoById(updateLiveInfo);
                        break;
                    }else if(LiveInfo.LIVE_STATUS_DONE.equals(newLiveStatus)){
                        //直播结束，更新状态
                        updateLiveInfo.setViewCount(Integer.valueOf(newInfo.get("viewCount")));
                        updateLiveInfo.setLikeCount(newInfo.get("likeCount"));
                        updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
                        liveInfoService.updateLiveInfoById(updateLiveInfo);
                        logger.info("直播已结束，下载弹幕信息完成。当前时间：" + DateUtil.getNowDateTime());
                        break;
                    }else if(LiveInfo.LIVE_STATUS_LIVEING.equals(newLiveStatus)){
                        logger.error("下载出错，第" + (i + 1) + "次，正在重试。当前时间：" + DateUtil.getNowDateTime());
                        if(i == Constant.DOWNLOAD_FAILURE_RETRY_COUNT - 1){
                            //多次失败
                            updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_FILURE);
                            liveInfoService.updateLiveInfoById(updateLiveInfo);
                            logger.error("下载失败，重试次数" + Constant.DOWNLOAD_FAILURE_RETRY_COUNT + "。当前时间：" + DateUtil.getNowDateTime());
                        }
                    }
                }
            }).start();
        }
        return "1";
    }
}









