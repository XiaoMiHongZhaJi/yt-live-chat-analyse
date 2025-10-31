package com.lwf.ytlivechatanalyse.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.*;
import com.lwf.ytlivechatanalyse.service.SrtDataService;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/srtInfo")
public class SrtDataController {

    @Autowired
    SrtDataService srtInfoService;

    @RequestMapping("/queryList")
    public Result<SrtData> queryList(SrtData srtData, int limit, int page){
        PageHelper.startPage(page, limit);
        return new Result<>(new PageInfo<>(srtInfoService.selectSrtInfo(srtData)));
    }

    @RequestMapping("/querySrtDetail")
    public Result<SrtData> querySrtDetail(SrtData srtData, int limit){
        return new Result<>(srtInfoService.selectSrtDetail(srtData, limit));
    }
}
