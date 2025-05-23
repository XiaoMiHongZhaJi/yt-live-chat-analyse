package com.lwf.ytlivechatanalyse.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.*;
import com.lwf.ytlivechatanalyse.service.EmotesDataService;
import com.lwf.ytlivechatanalyse.service.LiveChatDataService;
import com.lwf.ytlivechatanalyse.service.LiveInfoService;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/liveChat")
public class LiveChatController {

    @Autowired
    LiveChatDataService liveChatDataService;

    @Autowired
    EmotesDataService emotesDataService;

    @Autowired
    LiveInfoService liveInfoService;

    @RequestMapping("/queryList")
    public Result queryList(LiveChatData liveChatData, String liveStatus, int limit, int page){
        limit = Math.min(limit, Constant.MAX_PAGE_SIZE);
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
