package com.lwf.ytlivechatanalyse.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.service.EmotesDataService;
import com.lwf.ytlivechatanalyse.service.LiveChatDataService;
import com.lwf.ytlivechatanalyse.service.LiveInfoService;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public Result<LiveChatData> queryList(LiveChatData liveChatData, String liveStatus){
        int limit = liveChatData.getLimit();
        limit = limit > Constant.MAX_PAGE_SIZE ? Constant.MAX_PAGE_SIZE : limit;
        PageHelper.startPage(liveChatData.getPage(),limit);
        if(liveStatus == null || LiveInfo.LIVE_STATUS_DONE.equals(liveStatus)){
            return new Result<>(new PageInfo<>(liveChatDataService.selectList(liveChatData, false)));
        }
        return new Result<>(new PageInfo<>(liveChatDataService.selectLivingList(liveChatData, false)));
    }

    @RequestMapping("/queryEmotes")
    public List<EmotesData> queryEmotes(){
        List<EmotesData> emotesData = emotesDataService.selectAll();
        return emotesData;
    }
}
