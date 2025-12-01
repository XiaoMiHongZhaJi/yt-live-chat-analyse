package com.lwf.ytlivechatanalyse.controller;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.*;
import com.lwf.ytlivechatanalyse.service.EmotesDataService;
import com.lwf.ytlivechatanalyse.service.LiveChatDataService;
import com.lwf.ytlivechatanalyse.service.LiveInfoService;
import com.lwf.ytlivechatanalyse.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/liveChat")
public class LiveChatController {

    @Autowired
    LiveChatDataService liveChatDataService;

    @Autowired
    EmotesDataService emotesDataService;

    @Autowired
    LiveInfoService liveInfoService;

    @RequestMapping("/addLiveChat")
    @PreAuthorize("principal.userName == 'admin'")
    public Result addLiveChat(@RequestBody LiveChatData liveChatData){
        if(liveChatData == null || StringUtils.isEmpty(liveChatData.getMessage())){
            return new Result<>("empty");
        }
        liveChatDataService.addLiveChat(liveChatData);
        return new Result<>("success");
    }

    @RequestMapping("/addLiveChatJson")
    @PreAuthorize("principal.userName == 'admin'")
    public Result addLiveChatJson(MultipartFile file){
        //从文件导入
        if(file == null){
            return new Result<>(500, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if(filename == null || !filename.endsWith(".json")){
            return new Result<>(500, "只能导入json文件");
        }
        byte[] bytes = null;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            return new Result<>(500, "读取失败");
        }
        String jsonString = new String(bytes);
        if(StringUtils.isEmpty(jsonString)){
            return new Result<>(500, "读取失败");
        }
        List<LiveChatData> liveChatDataList = JSONArray.parseArray(jsonString, LiveChatData.class);
        for (LiveChatData liveChatData : liveChatDataList) {
            if(liveChatData == null || StringUtils.isEmpty(liveChatData.getMessage())){
                continue;
            }
            liveChatDataService.addLiveChat(liveChatData);
        }
        return new Result<>(200, "导入完成，导入弹幕数：" + liveChatDataList.size());
    }

    @RequestMapping("/queryList")
    public Result queryList(LiveChatData liveChatData, String liveStatus, int limit, int page){
        if(liveStatus == null || LiveInfo.LIVE_STATUS_DONE.equals(liveStatus)){
            PageHelper.startPage(page, limit);
            List<LiveChatData> liveChatList = liveChatDataService.selectList(liveChatData, false);
            if(!CollectionUtils.isEmpty(liveChatList)){
                return new Result<>(new PageInfo<>(liveChatList));
            }
        }
        PageHelper.startPage(page, limit);
        return new Result<>(new PageInfo<>(liveChatDataService.selectLivingList(liveChatData, false)));
    }

    @RequestMapping("/queryLiveChatDetail")
    public Result<LiveChatData> queryLiveChatDetail(LiveChatData liveChatData, int limit){
        return new Result<>(liveChatDataService.selectLiveChatDetail(liveChatData, limit));
    }

    @RequestMapping("/queryEmotes")
    public List<EmotesData> queryEmotes(){
        return emotesDataService.selectAll();
    }
}
