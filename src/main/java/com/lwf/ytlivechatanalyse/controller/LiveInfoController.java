package com.lwf.ytlivechatanalyse.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.BulletConfig;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public Result addLiveInfo(HttpServletRequest request, HttpServletResponse response, LiveInfo liveInfo, boolean downLiveChat, boolean getLiveInfo, MultipartFile file){
        if(!AuthUtil.auth(request)){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("WWW-Authenticate", "Basic realm=\"Realm\"");
            return new Result<>(500, "认证失败");
        }
        liveInfoService.addLiveInfo(liveInfo, downLiveChat, getLiveInfo);
        //从文件导入
        if(file != null){
            String filename = file.getOriginalFilename();
            if(filename != null && filename.endsWith("json")){
                liveChatDataService.importJsonFile(file, liveInfo.getLiveDate());
            }
        }
        return new Result<>(200, "新增成功");
    }

    @RequestMapping("/downloadBullet")
    public Result downloadBullet(String liveDate, String startTime, BulletConfig config){
        if(StringUtils.isBlank(liveDate) || liveDate.length() < 10){
            logger.error("liveDate错误{}，{}，{}", liveDate, startTime, config);
            return new Result(500, "输入的liveDate有误，正确的格式为：2023-01-01");
        }
        return liveInfoService.getBulletFile(liveDate, startTime, config);
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
    public Result<LiveInfo> queryList(HttpServletRequest request, HttpServletResponse response, int page, int limit, String year){
        if(!AuthUtil.auth(request)){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("WWW-Authenticate", "Basic realm=\"Realm\"");
            return new Result<>(500, "认证失败");
        }
        limit = Math.min(limit, Constant.MAX_PAGE_SIZE);
        PageHelper.startPage(page, limit);
        return new Result<>(new PageInfo<>(liveInfoService.selectList(year)));
    }

    @RequestMapping("/queryPrevLiveInfo")
    public String queryLastLiveDate(String liveDate){
        Date parseDate = new Date();
        try {
            parseDate = DateUtils.parseDate(liveDate.substring(0, 10), "yyyy-MM-dd");
        }catch (Exception e){
            logger.error("转换日期失败，" + liveDate, e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseDate);
        calendar.add(Calendar.DATE, -7);
        return DateFormatUtils.format(calendar, "yyyy-MM-dd");
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
    public Map<String,String> getLiveInfo(String url, String cookie){
        return CurlUtil.getLiveInfo(url, cookie);
    }

    @RequestMapping("/downloadChatData")
    public Result downloadChatData(HttpServletRequest request, HttpServletResponse response, LiveInfo liveInfo){
        if(!AuthUtil.auth(request)){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("WWW-Authenticate", "Basic realm=\"Realm\"");
            return new Result<>(500, "认证失败");
        }
        liveInfoService.downloadChatData(liveInfo);
        return new Result<>(200, "下载完成");
    }

    @RequestMapping("/selectCount")
    public Long selectCount(String liveDate){
        return srtDataService.selectCount(liveDate);
    }

    @RequestMapping("/importSrt")
    public Result importSrt(HttpServletRequest request, HttpServletResponse response, String liveDate, MultipartFile file){
        if(!AuthUtil.auth(request)){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("WWW-Authenticate", "Basic realm=\"Realm\"");
            return new Result<>(500, "认证失败");
        }
        //从文件导入
        if(file == null){
            return new Result<>(500, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if(filename == null || !filename.endsWith(".srt")){
            return new Result<>(500, "只能导入srt文件");
        }
        Long count = srtDataService.importSrt(liveDate, file);
        LiveInfo liveInfo = new LiveInfo();
        liveInfo.setLiveDate(liveDate);
        liveInfo.setSrtCount(count.intValue());
        liveInfoService.updateLiveInfoByDate(liveInfo);
        return new Result<>(200, "导入完成，当前字幕条数：" + count);
    }

    @RequestMapping("/stopDownload")
    public Result stopDownload(HttpServletRequest request, HttpServletResponse response, LiveInfo liveInfo){
        if(!AuthUtil.auth(request)){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("WWW-Authenticate", "Basic realm=\"Realm\"");
            return new Result<>(500, "认证失败");
        }
        String result = CmdUtil.kill("chat_downloader", liveInfo.getUrl());
        if(StringUtils.isBlank(result) || result.contains("未发现")){
            // 这种情况说明 chat_downloader 并未运行，只需要更新数据库状态即可
            liveInfo.setDownloadStatus(LiveInfo.DOWNLOAD_STATUS_DONE);
            liveInfo.setUpdateTime(new Date());
            liveInfoService.updateLiveInfoById(liveInfo);
            return new Result<>(200, "未在下载，已更新状态");
        }
        return new Result<>(200, "已结束并更新状态");
    }
}









