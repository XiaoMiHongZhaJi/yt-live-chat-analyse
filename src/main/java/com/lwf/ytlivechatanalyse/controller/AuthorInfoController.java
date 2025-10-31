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
    public List<AuthorInfo> queryListBySelector(String authorName, String schema){
        return authorInfoService.queryListBySelector(authorName, schema);
    }

    @RequestMapping("/queryList")
    public Result queryList(AuthorInfo authorInfo, String schema, int limit, int page){
        PageHelper.startPage(page, limit);
        return new Result<>(new PageInfo<>(authorInfoService.selectList(authorInfo, schema)));
    }

    @RequestMapping("/queryAuthorInfo")
    public AuthorInfo queryAuthorInfo(String authorId, String schema){
        return authorInfoService.queryAuthorInfo(authorId, schema);
    }

}
