package com.lwf.ytlivechatanalyse.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.service.EmotesDataService;
import com.lwf.ytlivechatanalyse.service.BlacklistService;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/blacklist")
public class BlacklistController {

    @Autowired
    BlacklistService blacklistService;

    @RequestMapping("/queryList")
    public Result<LiveChatData> queryList(LiveChatData queryData){
        int limit = queryData.getLimit();
        int page = queryData.getPage();
        limit = limit > Constant.MAX_PAGE_SIZE ? Constant.MAX_PAGE_SIZE : limit;
        PageHelper.startPage(page,limit);
        return new Result<>(new PageInfo<>(blacklistService.selectDesc(queryData)));
    }
}
