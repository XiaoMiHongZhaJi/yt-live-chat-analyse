package com.lwf.ytlivechatanalyse.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.BulletConfig;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.bean.LivingChatData;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
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
        String url = liveInfo.getUrl();
        if(StringUtils.isBlank(url)){
            //从文件导入
            if(file != null){
                String filename = file.getOriginalFilename();
                if(filename != null && filename.endsWith("json")){
                    String importResult = liveChatDataService.importJsonFile(file, liveInfo.getLiveDate());
                    return new Result<>(200, importResult);
                }
            }
            return new Result<>(500, "url不能为空");
        }
        String result;
        if(url.contains("you")){
            result = liveInfoService.addYoutubeLiveInfo(liveInfo, downLiveChat, getLiveInfo);
        }else{
            result = liveInfoService.addTwitchLiveInfo(liveInfo, downLiveChat);
        }
        if(StringUtils.isNotBlank(result)){
            return new Result<>(500, result);
        }
        return new Result<>(200, "新增成功");
    }

    @RequestMapping("/downloadBullet")
    public Result<String> downloadBullet(String liveDate, String startTime, Long startTimestamp, BulletConfig config){
        if(StringUtils.isBlank(liveDate) || liveDate.length() < 10){
            //未传入日期，尝试从 startTime 中获取日期
            if(StringUtils.isBlank(startTime)){
                //取最新日期
                LiveInfo liveInfo = liveInfoService.queryLastestLiveInfo();
                if (liveInfo == null || StringUtils.isBlank(liveInfo.getLiveDate())) {
                    return new Result<>(500, "传入的 liveDate 有误，正确的格式为：2023-01-01");
                }
                liveDate = liveInfo.getLiveDate();
                startTimestamp = liveInfo.getStartTimestamp();
            } else {
                if(startTime.length() < 10 || startTime.substring(0, 3).contains(":")){
                    logger.error("startTime 格式错误{}，{}", liveDate, startTime);
                    return new Result<>(500, "传入的 startTime 有误，正确的格式为：2023-01-01 20:40:00");
                }
                if(startTime.startsWith("202") && startTime.indexOf("-") == 4){
                    // 2023-01-01 20:40:00
                    liveDate = startTime.substring(0, 10);
                }else if(startTime.startsWith("2") && startTime.indexOf("-") == 2){
                    // 23-01-01 20:40:00
                    liveDate = "20" + startTime.substring(0, 8);
                }else if((startTime.startsWith("0") || startTime.startsWith("1"))&& startTime.indexOf("-") == 2){
                    // 01-01 20:40:00
                    liveDate = DateUtil.getNowDate().substring(0, 5) + startTime.substring(0, 5);
                }else{
                    logger.error("startTime 格式错误{}，{}", liveDate, startTime);
                    return new Result<>(500, "传入的 startTime 有误，正确的格式为：2023-01-01 20:40:00");
                }
            }
        }
        // 获取startTimestamp
        if (startTimestamp == null || startTimestamp == 0) {
            if(StringUtils.isBlank(startTime)){
                //未传入开始时间，尝试从数据库中获取开始时间
                LiveInfo liveInfo = new LiveInfo();
                liveInfo.setLiveDate(liveDate);
                liveInfo = liveInfoService.selectOne(liveInfo);
                if(liveInfo != null){
                    startTimestamp = liveInfo.getStartTimestamp();
                    Integer liveChatCount = liveInfo.getLiveChatCount();
                    String downloadStatus = liveInfo.getDownloadStatus();
                    if (LiveInfo.DOWNLOAD_STATUS_NONE.equals(downloadStatus) || liveChatCount == null || liveChatCount <= 0) {
                        logger.info("弹幕未下载，自动下载：{}", liveDate);
                        startTimestamp = liveInfoService.downloadLiveChat(liveInfo);
                    } else if (LiveInfo.DOWNLOAD_STATUS_DOWNLOADING.equals(downloadStatus)) {
                        return new Result<>(500, "弹幕下载中，请稍后再试");
                    } else if (LiveInfo.DOWNLOAD_STATUS_FILURE.equals(downloadStatus)) {
                        return new Result<>(500, "弹幕获取状态为失败");
                    }
                    if (startTimestamp == null || startTimestamp <= 0) {
                        return new Result<>(500, "获取到的 startTimestamp 为空");
                    }
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
            }else if(startTime.substring(0, 3).contains(":") && startTime.length() <= 8){
                //时间
                startTimestamp = DateUtil.getTimestamp(liveDate + startTime);
                logger.info("获取到转换的startTimestamp：{}", startTimestamp);
            }else{
                //默认
                startTimestamp = DateUtil.getTimestamp(startTime);
                logger.info("获取到转换的startTimestamp：{}", startTimestamp);
            }
        }
        List<LiveChatData> chatList = liveInfoService.getLiveChatData(liveDate);
        if(CollectionUtils.isEmpty(chatList)){
            logger.warn("所选日期无弹幕数据：{} {}", liveDate, startTime);
            return new Result<>(500, "所选日期无弹幕数据");
        }
        if(startTimestamp == null || startTimestamp == 0){
            //logger.error("传入的 startTime 有误：{} {}", liveDate, startTime);
            //return new Result<>(500, "传入的 startTime 有误，正确的格式为：2023-01-01 20:40:00");
            startTimestamp = chatList.get(0).getTimestamp();
            logger.warn("获取不到startTimestamp，取第一条弹幕的timestamp：{} {}", liveDate, startTimestamp);
        }
        if(config == null){
            config = new BulletConfig();
        }
        List<String> result = liveInfoService.getBulletContent(chatList, startTimestamp, config);
        return new Result<>(200, "生成ass文件成功", result);
    }

    @RequestMapping("/downloadBulletFile")
    public ResponseEntity<Object> downloadBulletFile(String liveDate, String startTime, Long startTimestamp, String fontSize){
        BulletConfig config = new BulletConfig();
        if(StringUtils.isNotBlank(fontSize)){
            config.setFontSize(Integer.parseInt(fontSize));
        }
        Result<String> result = downloadBullet(liveDate, startTime, startTimestamp, config);
        int code = result.getCode();
        if(code != 200 || result.getData() == null){
            return ResponseEntity.status(500).contentType(MediaType.parseMediaType("text/plain; charset=utf-8")).body(result.getMsg());
        }
        List<String> data = result.getData();
        if(data.size() < 3){
            return ResponseEntity.status(500).contentType(MediaType.parseMediaType("text/plain; charset=utf-8")).body(data.get(0));
        }
        String fileName = data.get(1);
        File file = liveInfoService.getBulletFile(data.get(2), fileName);
        try {
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(file.toPath()));
            ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
            builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
            builder.header("Content-disposition", "attachment; filename=" + fileName);
            return builder.body(resource);
        }catch(Exception e){
            logger.error("下载弹幕文件失败 {} {}", liveDate, startTime, e);
            return ResponseEntity.status(500).contentType(MediaType.parseMediaType("text/plain; charset=utf-8")).body("下载弹幕文件失败：" + e.getMessage());
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
            String liveDate = liveInfo.getLiveDate();
            LiveInfo updateLiveInfo = new LiveInfo();
            updateLiveInfo.setId(liveInfo.getId());
            if(LiveInfo.LIVE_STATUS_DONE.equals(liveInfo.getLiveStatus())){
                //录像
                if(liveInfo.getLiveChatCount() == null) {
                    int liveChatCount = liveChatDataService.selectCount(liveDate);
                    liveInfo.setLiveChatCount(liveChatCount);
                    updateLiveInfo.setLiveChatCount(liveChatCount);
                }
            }else{
                int livingChatCount = liveChatDataService.selectLivingCount(liveDate);
                liveInfo.setLivingChatCount(livingChatCount);
                updateLiveInfo.setLivingChatCount(livingChatCount);
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
            logger.error("转换日期失败，{}", liveDate, e);
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
    public int updateLiveInfo(LiveInfo liveInfo){
        if(liveInfo == null || StringUtils.isBlank(liveInfo.getLiveDate())){
            throw new RuntimeException("更新失败：liveDate 不能为空");
        }
        String liveDate = liveInfo.getLiveDate();
        if(liveInfo.getId() == null){
            return liveInfoService.updateLiveInfoByDate(liveInfo);
        }
        if(liveInfo.getLiveChatCount() == null){
            liveInfo.setLiveChatCount(liveChatDataService.selectCount(liveDate));
        }
        if(liveInfo.getLivingChatCount() == null){
            liveInfo.setLivingChatCount(liveChatDataService.selectLivingCount(liveDate));
        }
        return liveInfoService.updateLiveInfoById(liveInfo);
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
        String errMsg = srtDataService.importSrt(liveDate, file);
        Long count = srtDataService.selectCount(liveDate);
        LiveInfo liveInfo = new LiveInfo();
        liveInfo.setLiveDate(liveDate);
        liveInfo.setSrtCount(count.intValue());
        liveInfoService.updateLiveInfoByDate(liveInfo);
        if(StringUtils.isNotBlank(errMsg)){
            return new Result<>(200, String.format("导入部分出错，当前字幕条数：%d\n错误位置：%s", count, errMsg));
        }
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
    @RequestMapping("/queryImgUrl")
    public ResponseEntity<String> getRedirectedImageUrl(@RequestParam String originalUrl) {
        if(StringUtils.isBlank(originalUrl)){
            return ResponseEntity.ok("");
        }
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();

        ResponseEntity<Void> response = restTemplate.exchange(originalUrl, HttpMethod.HEAD, entity, Void.class);

        if (response.getStatusCode() == HttpStatus.FOUND || response.getStatusCode() == HttpStatus.MOVED_PERMANENTLY) {
            String redirectedUrl = response.getHeaders().getLocation().toString();
            return ResponseEntity.ok(redirectedUrl);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
    }
}









