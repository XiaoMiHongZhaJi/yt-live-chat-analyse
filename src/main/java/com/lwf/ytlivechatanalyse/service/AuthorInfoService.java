package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lwf.ytlivechatanalyse.bean.AuthorInfo;
import com.lwf.ytlivechatanalyse.dao.AuthorInfoMapper;
import com.lwf.ytlivechatanalyse.interceptor.DynamicSchemaInterceptor;
import com.lwf.ytlivechatanalyse.util.Constant;
import com.lwf.ytlivechatanalyse.util.Result;
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
            int index = authorName.indexOf("「");
            if(index > -1){
                authorName = authorName.substring(0, index);
            }
            String keywords = authorName;
            queryWrapper.and(wrapper -> wrapper
                .like("last_author_name", keywords)
                .or()
                .like("all_author_names", keywords)
            );
        }
        queryWrapper.orderByDesc("last_timestamp");
        queryWrapper.select("author_id", "first_author_name", "last_author_name", "all_author_names", "author_image");
        queryWrapper.last("limit 5");
        queryWrapper.eq("blocked", 0);
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
        queryWrapper.eq("blocked", 0);
        if(StringUtils.isNotBlank(year) && !year.equals(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + year);
        }
        List<AuthorInfo> authorInfoList = authorInfoMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(authorInfoList)) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("last_author_name");
            if(StringUtils.isNotBlank(lastAuthorName)){
                WrapperUtil.keyWordsLike(queryWrapper, lastAuthorName, "all_author_names");
            }
            queryWrapper.eq("blocked", 0);
            if(StringUtils.isNotBlank(year) && !year.equals(Constant.DEFAULT_YEAR)){
                DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + year);
            }
            authorInfoList = authorInfoMapper.selectList(queryWrapper);
        }
        return authorInfoList;
    }

    public AuthorInfo queryAuthorInfo(String authorId, String year) {
        QueryWrapper<AuthorInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        if(StringUtils.isNotBlank(year) && !year.equals(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + year);
        }
        return authorInfoMapper.selectOne(queryWrapper);
    }
}
