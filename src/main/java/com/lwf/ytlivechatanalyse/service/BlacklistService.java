package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.dao.LiveChatDataMapper;
import com.lwf.ytlivechatanalyse.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class BlacklistService {

    @Autowired
    LiveChatDataMapper liveChatDataMapper;

    public List<LiveChatData> selectDesc(LiveChatData queryData){
        String keyWords = queryData.getKeyWords();
        String authorName = queryData.getAuthorName();
        String liveDate = queryData.getLiveDate();
        QueryWrapper<LiveChatData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(liveDate),"live_date",liveDate);
        queryWrapper.eq(StringUtils.isNotBlank(authorName),"author_name",authorName);
        queryWrapper.like(StringUtils.isNotBlank(keyWords),"message",keyWords);
        queryWrapper.orderByDesc("id");
        List<LiveChatData> dataList = liveChatDataMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dataList)){
            int limit = queryData.getLimit();
            int page = queryData.getPage();
            limit = limit > Constant.MAX_PAGE_SIZE ? Constant.MAX_PAGE_SIZE : limit;
            PageHelper.startPage(page,limit);
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(StringUtils.isNotBlank(liveDate),"live_date",liveDate);
            queryWrapper.like(StringUtils.isNotBlank(authorName),"author_name",authorName);
            queryWrapper.like(StringUtils.isNotBlank(keyWords),"message",keyWords);
            dataList = liveChatDataMapper.selectList(queryWrapper);
        }
        return dataList;
    }
}
