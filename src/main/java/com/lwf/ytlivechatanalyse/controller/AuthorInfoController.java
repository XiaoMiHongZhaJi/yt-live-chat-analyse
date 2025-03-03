package com.lwf.ytlivechatanalyse.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.AuthorInfo;
import com.lwf.ytlivechatanalyse.service.AuthorInfoService;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/authorInfo")
public class AuthorInfoController {

    @Autowired
    AuthorInfoService authorInfoService;

    @RequestMapping("/queryListBySelector")
    public List<AuthorInfo> queryListBySelector(String authorName, String year){
        return authorInfoService.queryListBySelector(authorName, year);
    }

    @RequestMapping("/queryList")
    public Result queryList(AuthorInfo authorInfo, String year, int limit, int page){
        limit = Math.min(limit, Constant.MAX_PAGE_SIZE);
        PageHelper.startPage(page, limit);
        return new Result<>(new PageInfo<>(authorInfoService.selectList(authorInfo, year)));
    }

}
