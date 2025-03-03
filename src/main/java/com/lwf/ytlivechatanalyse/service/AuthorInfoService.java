package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lwf.ytlivechatanalyse.bean.AuthorInfo;
import com.lwf.ytlivechatanalyse.dao.AuthorInfoMapper;
import com.lwf.ytlivechatanalyse.interceptor.DynamicSchemaInterceptor;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.WrapperUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorInfoService {

    private final Logger logger = LoggerFactory.getLogger(AuthorInfoService.class);

    @Autowired
    AuthorInfoMapper authorInfoMapper;

    public List<AuthorInfo> queryListBySelector(String authorName, String year){
        QueryWrapper<AuthorInfo> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(authorName)){
            queryWrapper.and(wrapper -> wrapper
                .like("last_author_name", authorName)
                .or()
                .like("first_author_name", authorName)
            );
        }
        queryWrapper.orderByDesc("last_timestamp");
        queryWrapper.select("author_id", "first_author_name", "last_author_name");
        queryWrapper.last("limit 5");
        if(StringUtils.isNotBlank(year) && !year.equals(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + year);
        }
        return authorInfoMapper.selectList(queryWrapper);
    }

    public List<AuthorInfo> selectList(AuthorInfo authorInfo, String year){
        QueryWrapper<AuthorInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("last_author_name");
        String lastAuthorName = authorInfo.getLastAuthorName();
        if(StringUtils.isNotBlank(lastAuthorName)){
            WrapperUtil.keyWordsLike(queryWrapper, lastAuthorName, "last_author_name");
        }
        if(StringUtils.isNotBlank(year) && !year.equals(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + year);
        }
        List<AuthorInfo> authorInfoList = authorInfoMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(authorInfoList)) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("last_author_name");
            if(StringUtils.isNotBlank(lastAuthorName)){
                WrapperUtil.keyWordsLike(queryWrapper, lastAuthorName, "first_author_name");
            }
            authorInfoList = authorInfoMapper.selectList(queryWrapper);
        }
        return authorInfoList;
    }

}
