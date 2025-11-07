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
@RequestMapping("/api/authorInfo")
public class AuthorInfoController {

    @Autowired
    AuthorInfoService authorInfoService;

    @RequestMapping("/queryListBySelector")
    public List<AuthorInfo> queryListBySelector(String authorName){
        return authorInfoService.queryListBySelector(authorName);
    }

    @RequestMapping("/queryList")
    public Result queryList(AuthorInfo authorInfo, int limit, int page){
        PageHelper.startPage(page, limit);
        return new Result<>(new PageInfo<>(authorInfoService.selectList(authorInfo)));
    }

    @RequestMapping("/queryAuthorInfo")
    public AuthorInfo queryAuthorInfo(String authorId){
        return authorInfoService.queryAuthorInfo(authorId);
    }

}
