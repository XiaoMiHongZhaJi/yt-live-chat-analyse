package com.lwf.ytlivechatanalyse.controller;

import com.lwf.ytlivechatanalyse.bean.HotList;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.service.EmotesDataService;
import com.lwf.ytlivechatanalyse.service.LiveChatDataService;
import com.lwf.ytlivechatanalyse.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/analyse")
public class AnalyseController {

    @Autowired
    LiveChatDataService liveChatDataService;

    @Autowired
    EmotesDataService emotesDataService;

    @RequestMapping("/queryHotList")
    public Result<HotList> queryHotList(LiveChatData liveChatData, Integer intervalMinutes, String liveStatus){
        if(StringUtils.isBlank(liveChatData.getLiveDate())){
            return new Result<>();
        }
        List<HotList> hotList = liveChatDataService.queryHotList(liveChatData, intervalMinutes, liveStatus);
        return new Result<>(hotList);
    }

    @RequestMapping("/queryHotListDetail")
    public Result<LiveChatData> queryHotListDetail(String liveDate, String startTime, Integer intervalMinutes){
        List<LiveChatData> chatInfoKList = liveChatDataService.queryHotListDetail(liveDate, startTime, intervalMinutes);
        return new Result<>(chatInfoKList);
    }

}
