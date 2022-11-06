package com.lwf.ytlivechatanalyse.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.service.LiveChatDataService;
import com.lwf.ytlivechatanalyse.service.LiveInfoService;
import com.lwf.ytlivechatanalyse.service.SrtDataService;
import com.lwf.ytlivechatanalyse.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Autowired
    SrtDataService srtDataService;

    @PostMapping("/addLiveInfo")
    public void addLiveInfo(LiveInfo liveInfo, boolean downLiveChat, boolean getLiveInfo, MultipartFile file){
        liveInfoService.addLiveInfo(liveInfo, downLiveChat, getLiveInfo);
        //从文件导入
        if(file != null){
            String filename = file.getOriginalFilename();
            if(filename.endsWith(".xml")){

            }else if(filename.endsWith("json")){
                liveChatDataService.importJsonFile(file, liveInfo.getLiveDate());
            }
        }
    }

    @RequestMapping("/queryListBySelector")
    public List<LiveInfo> queryListBySelector(LiveInfo liveInfo){
        return liveInfoService.queryListBySelector(liveInfo);
    }

    @RequestMapping("/queryListById")
    public List<LiveInfo> queryListById(String ids){
        //用于定时器更新
        List<LiveInfo> liveInfoList = liveInfoService.queryListById(ids);
        for(LiveInfo liveInfo : liveInfoList){
            LiveInfo updateLiveInfo = new LiveInfo();
            updateLiveInfo.setId(liveInfo.getId());
            if(LiveInfo.LIVE_STATUS_DONE.equals(liveInfo.getLiveStatus())){
                //录像
                liveInfo.setLiveChatCount(liveChatDataService.selectCount(liveInfo.getLiveDate()));
                updateLiveInfo.setLiveChatCount(liveInfo.getLiveChatCount());
            }else{
                liveInfo.setLivingChatCount(liveChatDataService.selectLivingCount(liveInfo.getLiveDate()));
                updateLiveInfo.setLivingChatCount(liveInfo.getLivingChatCount());
            }
            liveInfoService.updateLiveInfoById(updateLiveInfo);
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
        String liveDate = liveInfo.getLiveDate();
        if(StringUtils.isNotBlank(liveDate)){
            liveInfo.setLiveChatCount(liveChatDataService.selectCount(liveDate));
            liveInfo.setLivingChatCount(liveChatDataService.selectLivingCount(liveDate));
        }
        liveInfoService.updateLiveInfoById(liveInfo);
    }

    @RequestMapping("/getLiveInfo")
    public Map<String,String> getLiveInfo(String url){
        Map<String, String> info = CurlUtil.getLiveInfo(url);
        return info;
    }

    @RequestMapping("/downloadChatData")
    public void downloadChatData(LiveInfo liveInfo){
        liveInfoService.downloadChatData(liveInfo);
    }

    @RequestMapping("/selectCount")
    public Long selectCount(String liveDate){
        return srtDataService.selectCount(liveDate);
    }

    @RequestMapping("/importSrt")
    public void importSrt(String liveDate, MultipartFile file){
        //从文件导入
        if(file == null){
            throw new RuntimeException("文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if(filename.endsWith(".srt")){
            srtDataService.importSrt(liveDate, file);
        }
    }

    @RequestMapping("/stopDownload")
    public String stopDownload(LiveInfo liveInfo){
        String result = CmdUtil.kill("chat_downloader", liveInfo.getUrl());
        if(result.contains("未发现")){
            // 这种情况说明 chat_downloader 并未运行，只需要更新数据库状态即可
            liveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
            liveInfo.setUpdateTime(new Date());
            liveInfoService.updateLiveInfoById(liveInfo);
            return "已更新状态";
        }
        return "已停止下载";
    }
}









