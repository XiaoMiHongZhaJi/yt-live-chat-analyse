package com.lwf.ytlivechatanalyse.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.service.LiveChatDataService;
import com.lwf.ytlivechatanalyse.service.LiveInfoService;
import com.lwf.ytlivechatanalyse.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/liveInfo")
public class LiveInfoController {

    @Autowired
    LiveChatDataService liveChatDataService;

    @Autowired
    LiveInfoService liveInfoService;

    @PostMapping("/addLiveInfo")
    public int addLiveInfo(LiveInfo liveInfo, boolean downLiveChat, boolean getLiveInfo, MultipartFile file){
        Long count = liveInfoService.selectCountByUrl(liveInfo.getUrl());
        if(count > 0){
            return 0;
        }
        if(getLiveInfo){
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
            liveInfo.setLiveDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        }
        liveInfoService.insert(liveInfo);
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
    public List<LiveInfo> queryListById(String id){
        //用于定时器更新
        List<LiveInfo> liveInfoList = liveInfoService.queryListById(id);
        for(LiveInfo liveInfo : liveInfoList){
            if(LiveInfo.DOWNLOAD_STATUS_DOWNLOADING.equals(liveInfo.getDownloadStatus())){
                //下载中
                Long count = liveChatDataService.selectCount(liveInfo);
                liveInfo.setChatCount(Math.toIntExact(count));
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

    @RequestMapping("/getLiveInfoByUrl")
    public Map<String,String> getLiveInfoByUrl(String url){
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
            //删除已有的
            liveChatDataService.deleteByLiveDate(liveDate);
        }
        new Thread(() ->{
            CmdUtil.execCmd("chat_downloader " + url, "GBK", true, false);
            //下载完成
            Long count = liveChatDataService.selectCount(liveInfo);
            updateLiveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
            updateLiveInfo.setLiveStatus(LiveInfo.LIVE_STATUS_DONE);
            updateLiveInfo.setChatCount(Math.toIntExact(count));
            updateLiveInfo.setUpdateTime(new Date());
            liveInfoService.updateLiveInfoById(updateLiveInfo);
        }).start();
        return "1";
    }
}









