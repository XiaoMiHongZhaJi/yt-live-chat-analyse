package com.lwf.ytlivechatanalyse.controller;

import com.lwf.ytlivechatanalyse.bean.HotList;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.service.AnalyseService;
import com.lwf.ytlivechatanalyse.service.EmotesDataService;
import com.lwf.ytlivechatanalyse.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analyse")
public class AnalyseController {

    @Autowired
    AnalyseService analyseService;

    @Autowired
    EmotesDataService emotesDataService;

    @RequestMapping("/queryHotList")
    public Result<HotList> queryHotList(LiveInfo liveInfo, Integer intervalMinutes, String keyword){
        if(StringUtils.isBlank(liveInfo.getLiveDate())){
            return new Result<>();
        }
        List<HotList> hotList = analyseService.queryHotList(liveInfo, intervalMinutes, keyword);
        return new Result<>(hotList);
    }

    @RequestMapping("/queryHotListDetail")
    public Result queryHotListDetail(String liveDate, String schema, Long startTimestamp, Integer intervalMinutes){
        List hotListDetail = analyseService.queryHotListDetail(liveDate, schema, startTimestamp, intervalMinutes);
        return new Result<>(hotListDetail);
    }

}
